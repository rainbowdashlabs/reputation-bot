"""
Frontend localization fixer.

Provides automatic fixes for common localization issues:
- Fix unused keys: Delete unused keys from all locale files
- Fix extra keys: Remove keys from other languages not in en-US
- Fix missing keys: Add missing keys to en-US only
"""

from pathlib import Path
from typing import Dict, Set, List

from .common import (
    load_locale_file,
    save_locale_file,
    get_all_locale_codes,
    flatten_keys,
    unflatten_keys,
    extract_translation_keys_from_code
)


class LocaleFixer:
    """Fixer for frontend localization files."""
    
    def __init__(self, project_root: Path):
        self.project_root = project_root
        self.frontend_src = project_root / "frontend" / "src"
        self.locales_dir = self.frontend_src / "locales"
        self.reference_locale = "en-US"
    
    def fix_unused_keys(self, unused_keys: Set[str], dry_run: bool = True) -> List[str]:
        """
        Fix tool for check 2: Delete unused keys from all locale files.
        """
        actions = []
        locale_codes = get_all_locale_codes(self.locales_dir)
        
        for locale_code in locale_codes:
            locale_data = load_locale_file(self.locales_dir, locale_code)
            flat_locale = flatten_keys(locale_data)
            
            # Remove unused keys
            keys_to_remove = unused_keys & set(flat_locale.keys())
            if keys_to_remove:
                for key in keys_to_remove:
                    del flat_locale[key]
                
                sorted_keys = sorted(keys_to_remove)
                actions.append(f"Removed {len(keys_to_remove)} unused keys from '{locale_code}': {sorted_keys}")
                
                if not dry_run:
                    # Convert back to nested structure and save
                    nested_data = unflatten_keys(flat_locale)
                    save_locale_file(self.locales_dir, locale_code, nested_data)
        
        return actions
    
    def fix_extra_keys(self, extra_keys_by_locale: Dict[str, Set[str]], dry_run: bool = True) -> List[str]:
        """
        Fix tool for check 4: Remove keys from other languages not in en-US.
        """
        actions = []
        
        for locale_code, extra_keys in extra_keys_by_locale.items():
            locale_data = load_locale_file(self.locales_dir, locale_code)
            flat_locale = flatten_keys(locale_data)
            
            # Remove extra keys
            for key in extra_keys:
                if key in flat_locale:
                    del flat_locale[key]
            
            actions.append(f"Removed {len(extra_keys)} extra keys from '{locale_code}'")
            
            if not dry_run:
                # Convert back to nested structure and save
                nested_data = unflatten_keys(flat_locale)
                save_locale_file(self.locales_dir, locale_code, nested_data)
        
        return actions
    
    def fix_missing_keys_in_reference(self, dry_run: bool = True) -> List[str]:
        """
        Fix tool for check 1: Add missing keys to en-US only.
        Note: Wildcard patterns are ignored since we only add concrete keys.
        """
        actions = []
        
        # Get keys used in code (wildcard patterns are ignored for fixing)
        used_keys, _ = extract_translation_keys_from_code(self.frontend_src)
        
        # Get keys in en-US
        reference_data = load_locale_file(self.locales_dir, self.reference_locale)
        flat_reference = flatten_keys(reference_data)
        reference_keys = set(flat_reference.keys())
        
        # Find missing keys
        missing_keys = used_keys - reference_keys
        
        if missing_keys:
            # Add missing keys with placeholder values
            for key in sorted(missing_keys):
                flat_reference[key] = f"TODO: {key}"
            
            actions.append(f"Added {len(missing_keys)} missing keys to en-US: {sorted(missing_keys)}")
            
            if not dry_run:
                # Convert back to nested structure and save
                nested_data = unflatten_keys(flat_reference)
                save_locale_file(self.locales_dir, self.reference_locale, nested_data)
        else:
            actions.append("No missing keys to add to en-US")
        
        return actions
