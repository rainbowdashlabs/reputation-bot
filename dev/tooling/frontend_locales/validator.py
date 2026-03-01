"""
Frontend localization validator.

Validates frontend locale files for common issues:
1. Check that all language codes present in t(<code>) are in all locale files
2. Check that no unused keys exist in the en-US file
3. Check that every variable like {limit} exists in other languages as well
4. Check that other languages do not contain keys not in en-US
"""

from pathlib import Path
from typing import Dict, Set, List, Tuple

from .common import (
    load_locale_file,
    get_all_locale_codes,
    flatten_keys,
    extract_translation_keys_from_code,
    extract_variables,
    match_wildcard_patterns
)


class LocaleValidator:
    """Validator for frontend localization files."""
    
    def __init__(self, project_root: Path):
        self.project_root = project_root
        self.frontend_src = project_root / "frontend" / "src"
        self.locales_dir = self.frontend_src / "locales"
        self.reference_locale = "en-US"
    
    def check_all_keys_in_all_locales(self) -> Tuple[bool, List[str]]:
        """
        Check 1: All language codes present in t(<code>) are in all locale files.
        Returns (is_valid, list_of_issues)
        """
        issues = []
        
        # Get keys used in code (including wildcard patterns)
        used_keys, wildcard_patterns = extract_translation_keys_from_code(self.frontend_src)
        
        # Get all locale codes
        locale_codes = get_all_locale_codes(self.locales_dir)
        
        # Check each locale file
        for locale_code in locale_codes:
            locale_data = load_locale_file(self.locales_dir, locale_code)
            flat_locale = flatten_keys(locale_data)
            locale_keys = set(flat_locale.keys())
            
            # Find keys matched by wildcard patterns
            matched_by_wildcards = match_wildcard_patterns(wildcard_patterns, locale_keys)
            
            # Missing keys are those not directly present and not matched by wildcards
            missing_keys = used_keys - locale_keys
            if missing_keys:
                issues.append(f"Locale '{locale_code}' is missing keys: {sorted(missing_keys)}")
        
        return len(issues) == 0, issues
    
    def check_no_unused_keys(self) -> Tuple[bool, List[str], Set[str]]:
        """
        Check 2: No unused keys exist in the en-US file.
        Returns (is_valid, list_of_issues, set_of_unused_keys)
        """
        issues = []
        
        # Get keys used in code (including wildcard patterns)
        used_keys, wildcard_patterns = extract_translation_keys_from_code(self.frontend_src)
        
        # Get keys in en-US
        reference_data = load_locale_file(self.locales_dir, self.reference_locale)
        flat_reference = flatten_keys(reference_data)
        reference_keys = set(flat_reference.keys())
        
        # Find keys matched by wildcard patterns
        matched_by_wildcards = match_wildcard_patterns(wildcard_patterns, reference_keys)
        
        # Find unused keys (not directly used and not matched by wildcards)
        unused_keys = reference_keys - used_keys - matched_by_wildcards
        
        if unused_keys:
            issues.append(f"en-US has {len(unused_keys)} unused keys: {sorted(unused_keys)}")
        
        return len(unused_keys) == 0, issues, unused_keys
    
    def check_variables_consistency(self) -> Tuple[bool, List[str]]:
        """
        Check 3: Every variable like {limit} exists in other languages as well.
        Returns (is_valid, list_of_issues)
        """
        issues = []
        
        # Get reference locale
        reference_data = load_locale_file(self.locales_dir, self.reference_locale)
        flat_reference = flatten_keys(reference_data)
        
        # Extract variables from reference locale
        reference_variables = {}
        for key, value in flat_reference.items():
            variables = extract_variables(value)
            if variables:
                reference_variables[key] = variables
        
        # Check other locales
        locale_codes = [code for code in get_all_locale_codes(self.locales_dir) 
                       if code != self.reference_locale]
        
        for locale_code in locale_codes:
            locale_data = load_locale_file(self.locales_dir, locale_code)
            flat_locale = flatten_keys(locale_data)
            
            for key, ref_vars in reference_variables.items():
                if key in flat_locale:
                    locale_vars = extract_variables(flat_locale[key])
                    
                    missing_vars = ref_vars - locale_vars
                    extra_vars = locale_vars - ref_vars
                    
                    if missing_vars:
                        issues.append(
                            f"Locale '{locale_code}', key '{key}': missing variables {missing_vars}"
                        )
                    if extra_vars:
                        issues.append(
                            f"Locale '{locale_code}', key '{key}': extra variables {extra_vars}"
                        )
        
        return len(issues) == 0, issues
    
    def check_no_extra_keys_in_other_locales(self) -> Tuple[bool, List[str], Dict[str, Set[str]]]:
        """
        Check 4: Other languages do not contain keys not in en-US.
        Returns (is_valid, list_of_issues, dict_of_extra_keys_by_locale)
        """
        issues = []
        extra_keys_by_locale = {}
        
        # Get reference keys
        reference_data = load_locale_file(self.locales_dir, self.reference_locale)
        flat_reference = flatten_keys(reference_data)
        reference_keys = set(flat_reference.keys())
        
        # Check other locales
        locale_codes = [code for code in get_all_locale_codes(self.locales_dir) 
                       if code != self.reference_locale]
        
        for locale_code in locale_codes:
            locale_data = load_locale_file(self.locales_dir, locale_code)
            flat_locale = flatten_keys(locale_data)
            locale_keys = set(flat_locale.keys())
            
            extra_keys = locale_keys - reference_keys
            if extra_keys:
                extra_keys_by_locale[locale_code] = extra_keys
                issues.append(
                    f"Locale '{locale_code}' has {len(extra_keys)} extra keys not in en-US: {sorted(extra_keys)}"
                )
        
        return len(issues) == 0, issues, extra_keys_by_locale
    
    def run_all_checks(self) -> bool:
        """Run all validation checks and report results."""
        print("=" * 80)
        print("Frontend Localization Validation")
        print("=" * 80)
        print()
        
        all_valid = True
        
        # Check 1
        print("Check 1: All translation keys used in code exist in all locale files")
        print("-" * 80)
        valid, issues = self.check_all_keys_in_all_locales()
        if valid:
            print("✓ PASS: All keys are present in all locale files")
        else:
            print("✗ FAIL: Some keys are missing")
            for issue in issues:
                print(f"  - {issue}")
            all_valid = False
        print()
        
        # Check 2
        print("Check 2: No unused keys in en-US")
        print("-" * 80)
        valid, issues, unused_keys = self.check_no_unused_keys()
        if valid:
            print("✓ PASS: No unused keys in en-US")
        else:
            print("✗ FAIL: Unused keys found")
            for issue in issues:
                print(f"  - {issue}")
            all_valid = False
        print()
        
        # Check 3
        print("Check 3: Variables consistency across all languages")
        print("-" * 80)
        valid, issues = self.check_variables_consistency()
        if valid:
            print("✓ PASS: All variables are consistent")
        else:
            print("✗ FAIL: Variable inconsistencies found")
            for issue in issues:
                print(f"  - {issue}")
            all_valid = False
        print()
        
        # Check 4
        print("Check 4: No extra keys in other languages (not in en-US)")
        print("-" * 80)
        valid, issues, extra_keys = self.check_no_extra_keys_in_other_locales()
        if valid:
            print("✓ PASS: No extra keys in other languages")
        else:
            print("✗ FAIL: Extra keys found")
            for issue in issues:
                print(f"  - {issue}")
            all_valid = False
        print()
        
        print("=" * 80)
        if all_valid:
            print("✓ All checks passed!")
        else:
            print("✗ Some checks failed. Use --fix to automatically fix issues.")
        print("=" * 80)
        
        return all_valid
