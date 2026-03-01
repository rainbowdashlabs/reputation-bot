"""
Common utilities for frontend localization validation and fixing.
"""

import json
import re
from pathlib import Path
from typing import Dict, Set


def load_locale_file(locales_dir: Path, locale_code: str) -> Dict:
    """Load a locale JSON file."""
    locale_file = locales_dir / f"{locale_code}.json"
    if not locale_file.exists():
        return {}
    
    with open(locale_file, 'r', encoding='utf-8') as f:
        return json.load(f)


def save_locale_file(locales_dir: Path, locale_code: str, data: Dict):
    """Save a locale JSON file with proper formatting."""
    locale_file = locales_dir / f"{locale_code}.json"
    with open(locale_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write('\n')  # Add trailing newline


def get_all_locale_codes(locales_dir: Path) -> list:
    """Get all available locale codes from the locales directory."""
    locale_files = locales_dir.glob("*.json")
    return sorted([f.stem for f in locale_files])


def flatten_keys(data: Dict, prefix: str = "") -> Dict[str, str]:
    """Flatten nested JSON structure to dot-notation keys."""
    result = {}
    for key, value in data.items():
        full_key = f"{prefix}.{key}" if prefix else key
        if isinstance(value, dict):
            result.update(flatten_keys(value, full_key))
        else:
            result[full_key] = str(value)
    return result


def unflatten_keys(flat_data: Dict[str, str]) -> Dict:
    """Convert flat dot-notation keys back to nested structure."""
    result = {}
    for key, value in flat_data.items():
        parts = key.split('.')
        current = result
        for part in parts[:-1]:
            if part not in current:
                current[part] = {}
            current = current[part]
        current[parts[-1]] = value
    return result


def extract_translation_keys_from_code(frontend_src: Path) -> Set[str]:
    """Extract all translation keys used in the frontend code.
    
    Returns a set of keys, which may include wildcard patterns like 'key.prefix.*.suffix'
    where * represents a variable interpolation.
    """
    keys = set()
    wildcard_patterns = set()
    
    # Pattern to find t('key') or t("key") calls
    translation_pattern = re.compile(r"""\bt\(['"]([\w.]+)['"]\)""")
    
    # Pattern to find template string interpolations like t(`key.${variable}`)
    # Captures the key parts and converts ${variable} to a wildcard
    template_pattern = re.compile(r"""\bt\(`([a-zA-Z.]*)\$\{[^}]+\}([a-zA-Z.]*)`\)""")
    
    # Pattern to find simple string locale codes like 'key.name' or "key.name"
    # Matches quoted strings with at least one dot and alphanumeric characters
    simple_string_pattern = re.compile(r"""['"]([a-zA-Z]+\.[a-zA-Z.]+)['"]""")
    
    # Whitelist patterns to exclude false positives
    # These are common patterns that look like locale keys but aren't
    exclude_patterns = {
        # Object property paths
        r'.*\.id$',
        r'.*\.name$',
        r'.*\.code$',
        r'.*\.key$',
        r'.*\.path$',
        r'.*\.roleIds?$',
        r'.*\.internalName$',
        # Session/settings paths
        r'^session\.settings\..*',
        # Enum-like patterns (all caps after dot)
        r'.*\.[A-Z_]+$',
    }
    
    def should_exclude(key: str) -> bool:
        """Check if a key matches any exclusion pattern."""
        for pattern in exclude_patterns:
            if re.match(pattern, key):
                return True
        return False
    
    # Search in .vue, .ts, .js files
    for pattern in ["**/*.vue", "**/*.ts", "**/*.js"]:
        for file_path in frontend_src.glob(pattern):
            # Skip node_modules and dist directories
            if 'node_modules' in str(file_path) or 'dist' in str(file_path):
                continue
            
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    
                    # Find t('key') patterns
                    matches = translation_pattern.findall(content)
                    keys.update(matches)
                    
                    # Find template string patterns like t(`key.${variable}`)
                    template_matches = template_pattern.findall(content)
                    for prefix, suffix in template_matches:
                        # Convert to wildcard pattern
                        # Remove trailing dot from prefix and leading dot from suffix to avoid double dots
                        prefix_clean = prefix.rstrip('.')
                        suffix_clean = suffix.lstrip('.')
                        
                        if prefix_clean and suffix_clean:
                            wildcard_pattern = f"{prefix_clean}\\..+?\\.{suffix_clean}"
                        elif prefix_clean:
                            wildcard_pattern = f"{prefix_clean}\\..+?"
                        elif suffix_clean:
                            wildcard_pattern = f".+?\\.{suffix_clean}"
                        else:
                            wildcard_pattern = ".+?"
                        wildcard_patterns.add(wildcard_pattern)
                    
                    # Find simple string patterns like 'key.name'
                    simple_matches = simple_string_pattern.findall(content)
                    # Filter out false positives
                    filtered_matches = {m for m in simple_matches if not should_exclude(m)}
                    keys.update(filtered_matches)
            except Exception as e:
                print(f"Warning: Could not read {file_path}: {e}")
    
    return keys, wildcard_patterns


def match_wildcard_patterns(wildcard_patterns: Set[str], locale_keys: Set[str]) -> Set[str]:
    """Match wildcard patterns against locale keys.
    
    Args:
        wildcard_patterns: Set of regex patterns like 'prefix.+?suffix'
        locale_keys: Set of actual locale keys to match against
        
    Returns:
        Set of locale keys that match any wildcard pattern
    """
    matched_keys = set()
    for pattern in wildcard_patterns:
        regex = re.compile(f"^{pattern}$")
        for key in locale_keys:
            if regex.match(key):
                matched_keys.add(key)
    return matched_keys


def extract_variables(text: str) -> Set[str]:
    """Extract variable names like {limit} from a translation string."""
    variable_pattern = re.compile(r"\{(\w+)\}")
    return set(variable_pattern.findall(text))
