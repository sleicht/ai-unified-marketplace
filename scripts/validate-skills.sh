#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

ruby scripts/validate-skills.rb
python3 aiup-core/skills/use-case-spec/scripts/validate_use_case.py --self-test
python3 aiup-core/skills/use-case-spec/scripts/validate_use_case.py --strict \
  aiup-core/skills/use-case-spec/references/example.md

compile_dir="$(mktemp -d)"
trap 'rm -rf "$compile_dir"' EXIT
kotlinc scripts/fixtures/ApiClientConstruction.kt -d "$compile_dir/example.jar"

if rg -n 'localhost|DEFAULT_POC_EMPLOYEE_TOKEN' \
  aiup-compose-ktor-exposed/skills/compose-test/references/ExampleScreenTest.kt; then
  echo "Stale UI client example found" >&2
  exit 1
fi

if rg -n 'ServiceApiClient\(\)' \
  aiup-compose-ktor-exposed/skills/implement-ui/SKILL.md \
  aiup-compose-ktor-exposed/skills/implement-ui/references/ui-style.md; then
  echo "Stale UI client example found" >&2
  exit 1
fi

if rg -n 'accessTokenProvider\.accessToken\(\)|override suspend fun accessToken\(' \
  aiup-compose-ktor-exposed/skills/implement-ui \
  aiup-compose-ktor-exposed/skills/compose-test/references \
  scripts/fixtures; then
  echo "Stale access-token provider contract found" >&2
  exit 1
fi

if rg -n 'partnerContractNumber|PatientListItem' \
  aiup-compose-ktor-exposed/skills/compose-test/references/ExampleScreenTest.kt; then
  echo "Stale domain-specific UI example found" >&2
  exit 1
fi

echo "Documentation, use-case, and Kotlin example validation passed"
