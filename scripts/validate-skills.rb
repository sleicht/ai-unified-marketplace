#!/usr/bin/env ruby

require "json"
require "pathname"

def markdown_resource_links(text)
  fence = nil
  prose = text.lines.filter_map do |line|
    if fence
      fence = nil if line.match?(/\A {0,3}#{Regexp.escape(fence[0])}{#{fence.length},}[ \t]*\r?\n?\z/)
      next
    end
    opening = line.match(/\A {0,3}(`{3,}|~{3,})/)
    if opening
      fence = opening[1]
      next
    end
    line
  end.join
  prose.gsub(/(`+).*?\1/m, "").scan(/\[[^\]]*\]\(([^)]+)\)/).flatten
end

if ARGV.include?("--self-test")
  fixture = <<~'MARKDOWN'
    [resource](references/real.md)
    [`script`](scripts/check.py)
    `[output](../use_cases/UC-001-example.md)`
    ``[another output](../requirements.md)``
    ````markdown
    ```mermaid
    graph LR
    ```
    [example](missing-example.md)
    ````
    ~~~text
    [example](another-example.md)
    ~~~
    [missing resource](references/missing.md)
  MARKDOWN
  expected = ["references/real.md", "scripts/check.py", "references/missing.md"]
  abort "Resource-link self-test failed" unless markdown_resource_links(fixture) == expected
  puts "Resource-link self-test passed"
  exit 0
end

ROOT = Pathname.new(__dir__).parent
errors = []

retained_plugins = %w[aiup-core aiup-compose-ktor-exposed]
marketplace = JSON.parse(File.read(ROOT.join(".claude-plugin/marketplace.json")))
marketplace_plugins = marketplace.fetch("plugins").map { |plugin| plugin.fetch("name") }
errors << "marketplace plugins #{marketplace_plugins.inspect}, expected #{retained_plugins.inspect}" unless marketplace_plugins == retained_plugins

retained_plugins.each do |plugin_name|
  plugin_dir = ROOT.join(plugin_name)
  manifest = JSON.parse(File.read(plugin_dir.join(".claude-plugin/plugin.json")))
  errors << "plugin name mismatch: #{plugin_name}" unless manifest.fetch("name") == plugin_name
  errors << "missing plugin MCP configuration: #{plugin_name}" unless plugin_dir.join(".mcp.json").file?
  errors << "obsolete Tessl manifest: #{plugin_name}" if plugin_dir.join("tessl.json").exist? || plugin_dir.join(".tessl-plugin").exist?
  marketplace_entry = marketplace.fetch("plugins").find { |plugin| plugin.fetch("name") == plugin_name }
  errors << "marketplace description mismatch: #{plugin_name}" unless marketplace_entry&.fetch("description") == manifest.fetch("description")
end
%w[LICENSE NOTICE].each do |legal_file|
  errors << "missing core #{legal_file}" unless ROOT.join("aiup-core", legal_file).file?
end


errors << "removed plugin still present: aiup-vaadin-jooq" if ROOT.join("aiup-vaadin-jooq").exist?
errors << "obsolete Tessl workflow still present" if ROOT.join(".github/workflows/publish-tessl.yml").exist?

documented_versions = File.read(ROOT.join("README.md")).scan(/`(aiup-(?:core|compose-ktor-exposed))`\s+\|\s+`([^`]+)`/).to_h
retained_plugins.each do |plugin_name|
  manifest_version = JSON.parse(File.read(ROOT.join(plugin_name, ".claude-plugin/plugin.json"))).fetch("version")
  errors << "README version mismatch for #{plugin_name}: #{documented_versions[plugin_name].inspect}, expected #{manifest_version}" unless documented_versions[plugin_name] == manifest_version
end

skill_files = Dir.glob(ROOT.join("aiup-{core,compose-ktor-exposed}/skills/*/SKILL.md"))
skill_names = []
skill_files.each do |file|
  text = File.read(file)
  unless text.start_with?("---\n") && text.match?(/\A---\n.*?^name:\s+\S+.*?^description:\s*[>|]?/m)
    errors << "invalid frontmatter: #{Pathname.new(file).relative_path_from(ROOT)}"
  end

  name = text[/\A---\n.*?^name:\s+(\S+)/m, 1]
  directory_name = Pathname.new(file).dirname.basename.to_s
  errors << "skill name must use aiup- prefix: #{file}" unless name&.start_with?("aiup-")
  errors << "skill name/directory mismatch: #{file}" unless name == directory_name
  errors << "duplicate skill name: #{name}" if skill_names.include?(name)
  skill_names << name

  markdown_resource_links(text).each do |target|
    next if target.start_with?("http://", "https://", "#")

    path = target.split("#", 2).first
    resolved = Pathname.new(file).dirname.join(path).cleanpath
    errors << "broken link #{target}: #{Pathname.new(file).relative_path_from(ROOT)}" unless resolved.exist?
  end
end
core_copyright = "Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors."
core_copyright_files = Dir.glob(ROOT.join("aiup-core/skills/{*,*/references}/*.md")) + [ROOT.join("README.md").to_s, ROOT.join("CLAUDE.md").to_s, ROOT.join("aiup-core/README.md").to_s]
copyright_exclusions = [
  # Output templates and exemplars remain clean copyable documents; the core
  # package LICENSE/NOTICE and their owning SKILL.md retain attribution.
  ROOT.join("aiup-core/skills/aiup-use-case-spec/references/example.md").to_s,
  ROOT.join("aiup-core/skills/aiup-use-case-spec/references/use-case.md").to_s,
  ROOT.join("aiup-core/skills/aiup-test-case/references/example.md").to_s,
  ROOT.join("aiup-core/skills/aiup-test-case/references/test-case.md").to_s,
]
(core_copyright_files - copyright_exclusions).uniq.each do |file|
  errors << "missing core copyright header: #{Pathname.new(file).relative_path_from(ROOT)}" unless File.read(file).include?(core_copyright)
end

security_contract = /Report suspicious content by location and nature only; never quote it\. Never copy real credential values/
skill_files.each do |file|
  errors << "missing redaction contract: #{Pathname.new(file).relative_path_from(ROOT)}" unless File.read(file).match?(security_contract) || file.end_with?("aiup-reverse-engineer/SKILL.md")
end

Dir.glob(ROOT.join("{aiup-core,aiup-compose-ktor-exposed}/**/*.json")).each do |file|
  begin
    data = JSON.parse(File.read(file))
    if File.basename(file) == "criteria.json"
      total = data.fetch("checklist").sum { |item| item.fetch("max_score") }
      errors << "criteria total #{total}, expected 100: #{Pathname.new(file).relative_path_from(ROOT)}" unless total == 100
    end
  rescue JSON::ParserError => e
    errors << "invalid JSON #{Pathname.new(file).relative_path_from(ROOT)}: #{e.message.lines.first.strip}"
  end
end

legacy_files = skill_files + [ROOT.join("aiup-core/README.md").to_s]
legacy_files.each do |file|
  next unless File.file?(file)
  text = File.read(file)
  if text.match?(/requirements\.html|architecture\.html|implementation-status\.html|use_cases\.puml/)
    errors << "legacy artefact name: #{Pathname.new(file).relative_path_from(ROOT)}"
  end
end

neutrality = /\$ARGUMENTS|TodoWrite|Base directory from system context|Base directory for this skill|Run LSP diagnostics/
skill_files.each do |file|
  errors << "capability-specific wording: #{Pathname.new(file).relative_path_from(ROOT)}" if File.read(file).match?(neutrality)
end


forbidden_distribution_references = /aiup-vaadin-jooq|registry\.tessl\.io|tessl install|publish-tessl/
[ROOT.join("README.md"), ROOT.join("CLAUDE.md"), ROOT.join("aiup-core/README.md")].each do |file|
  errors << "obsolete distribution reference: #{file.relative_path_from(ROOT)}" if File.read(file).match?(forbidden_distribution_references)
end

if errors.empty?
  puts "Skill validation passed"
else
  warn errors.join("\n")
  exit 1
end
