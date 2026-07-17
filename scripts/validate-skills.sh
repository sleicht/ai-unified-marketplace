#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

ruby scripts/validate-skills.rb

compile_dir="$(mktemp -d)"
trap 'rm -rf "$compile_dir"' EXIT
kotlinc aiup-compose-ktor-exposed/evals/compile/ApiClientConstruction.kt -d "$compile_dir/example.jar"

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
  aiup-compose-ktor-exposed/evals/compile; then
  echo "Stale access-token provider contract found" >&2
  exit 1
fi

if rg -n 'partnerContractNumber|PatientListItem' \
  aiup-compose-ktor-exposed/skills/compose-test/references/ExampleScreenTest.kt; then
  echo "Stale domain-specific UI example found" >&2
  exit 1
fi

echo "Documentation and Kotlin example validation passed"
