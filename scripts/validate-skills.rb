#!/usr/bin/env ruby

require "json"
require "pathname"

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
end

errors << "removed plugin still present: aiup-vaadin-jooq" if ROOT.join("aiup-vaadin-jooq").exist?
errors << "obsolete Tessl workflow still present" if ROOT.join(".github/workflows/publish-tessl.yml").exist?

documented_versions = File.read(ROOT.join("README.md")).scan(/`(aiup-(?:core|compose-ktor-exposed))` \| `([^`]+)`/).to_h
retained_plugins.each do |plugin_name|
  manifest_version = JSON.parse(File.read(ROOT.join(plugin_name, ".claude-plugin/plugin.json"))).fetch("version")
  errors << "README version mismatch for #{plugin_name}: #{documented_versions[plugin_name].inspect}, expected #{manifest_version}" unless documented_versions[plugin_name] == manifest_version
end

skill_files = Dir.glob(ROOT.join("aiup-{core,compose-ktor-exposed}/skills/*/SKILL.md"))
skill_files.each do |file|
  text = File.read(file)
  unless text.start_with?("---\n") && text.match?(/\A---\n.*?^name:\s+\S+.*?^description:\s*[>|]?/m)
    errors << "invalid frontmatter: #{Pathname.new(file).relative_path_from(ROOT)}"
  end

  text.scan(/\[[^\]]+\]\(([^)]+)\)/).flatten.each do |target|
    next if target.start_with?("http://", "https://", "#")

    path = target.split("#", 2).first
    resolved = Pathname.new(file).dirname.join(path).cleanpath
    errors << "broken link #{target}: #{Pathname.new(file).relative_path_from(ROOT)}" unless resolved.exist?
  end
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
