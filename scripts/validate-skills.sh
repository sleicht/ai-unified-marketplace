#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

ruby scripts/validate-skills.rb --self-test
ruby scripts/validate-skills.rb
python3 aiup-core/skills/aiup-use-case-spec/scripts/validate_use_case.py --self-test
python3 aiup-core/skills/aiup-use-case-spec/scripts/validate_use_case.py --strict \
  aiup-core/skills/aiup-use-case-spec/references/example.md

example_dir="aiup-compose-ktor-exposed/skills/aiup-implement/references/record-example"
"$example_dir/gradlew" -p "$example_dir" \
  spotlessCheck :shared:jvmTest :client:jvmTest :server:test :web:compileKotlinJs \
  --console=plain

echo "Documentation, use-case, and compiled Kotlin example validation passed"
