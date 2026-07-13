#!/usr/bin/env ruby

require "json"
require "pathname"

ROOT = Pathname.new(__dir__).parent
errors = []

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

legacy_files = skill_files +
  Dir.glob(ROOT.join("aiup-core/evals/scenario-{2,3,4,5,6,7,8,9,10,11}/**/*.{md,json,html}")) +
  [ROOT.join("aiup-core/README.md").to_s]
legacy_files.each do |file|
  next unless File.file?(file)
  text = File.read(file)
  if text.match?(/requirements\.md|use_cases\.puml/)
    errors << "legacy artefact name: #{Pathname.new(file).relative_path_from(ROOT)}"
  end
end

neutrality = /\$ARGUMENTS|TodoWrite|Base directory from system context|Base directory for this skill|Run LSP diagnostics/
skill_files.each do |file|
  errors << "capability-specific wording: #{Pathname.new(file).relative_path_from(ROOT)}" if File.read(file).match?(neutrality)
end

Dir.glob(ROOT.join("aiup-core/evals/**/*.html")).each do |file|
  text = File.read(file)
  ids = text.scan(/\bid=["']([^"']+)["']/).flatten
  duplicates = ids.tally.select { |_id, count| count > 1 }.keys
  errors << "duplicate HTML IDs #{duplicates.join(', ')}: #{Pathname.new(file).relative_path_from(ROOT)}" unless duplicates.empty?

  next unless File.basename(file) == "requirements.html"
  %w[functional-requirements non-functional-requirements constraints use-case-diagram].each do |id|
    count = ids.count(id)
    errors << "requirements section ##{id} count #{count}: #{Pathname.new(file).relative_path_from(ROOT)}" unless count == 1
  end
end

Dir.glob(ROOT.join("aiup-compose-ktor-exposed/skills/*/evals/evals.json")).each do |file|
  data = JSON.parse(File.read(file))
  errors << "fewer than two behavioural evals: #{Pathname.new(file).relative_path_from(ROOT)}" if data.fetch("evals").length < 2
end

expected_eval_skills = %w[flyway-migration implement implement-ui ktor-test compose-test implementation-status]
actual_eval_skills = Dir.glob(ROOT.join("aiup-compose-ktor-exposed/skills/*/evals/evals.json")).map { |file| JSON.parse(File.read(file)).fetch("skill_name") }
missing_eval_skills = expected_eval_skills - actual_eval_skills
errors << "missing behavioural evals: #{missing_eval_skills.join(', ')}" unless missing_eval_skills.empty?

if errors.empty?
  puts "Skill validation passed"
else
  warn errors.join("\n")
  exit 1
end
