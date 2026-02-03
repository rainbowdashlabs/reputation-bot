#!/usr/bin/env python3
"""
Main entry point for frontend localization validation and fix tool.

This script validates frontend locale files and provides fixes for common issues:
1. Check that all language codes present in t(<code>) are in all locale files
2. Check that no unused keys exist in the en-US file
3. Check that every variable like {limit} exists in other languages as well
4. Check that other languages do not contain keys not in en-US

Fix tools:
- Fix unused keys: Delete unused keys from all locale files
- Fix extra keys: Remove keys from other languages not in en-US
- Fix missing keys: Add missing keys to en-US only
"""

import sys
import argparse
from pathlib import Path

from .validator import LocaleValidator
from .fixer import LocaleFixer


def main():
    parser = argparse.ArgumentParser(
        description="Validate and fix frontend localization files"
    )
    parser.add_argument(
        '--fix',
        action='store_true',
        help='Automatically fix issues (default: dry-run only)'
    )
    parser.add_argument(
        '--fix-unused',
        action='store_true',
        help='Fix unused keys (delete from all locale files)'
    )
    parser.add_argument(
        '--fix-extra',
        action='store_true',
        help='Fix extra keys (remove from non-en-US locales)'
    )
    parser.add_argument(
        '--fix-missing',
        action='store_true',
        help='Fix missing keys (add to en-US only)'
    )
    parser.add_argument(
        '--project-root',
        type=Path,
        default=Path(__file__).parent.parent.parent,
        help='Path to project root (default: parent of script directory)'
    )
    
    args = parser.parse_args()
    
    validator = LocaleValidator(args.project_root)
    fixer = LocaleFixer(args.project_root)
    
    # If no specific fix is requested, run all checks
    if not (args.fix or args.fix_unused or args.fix_extra or args.fix_missing):
        all_valid = validator.run_all_checks()
        sys.exit(0 if all_valid else 1)
    
    # Run fixes
    dry_run = not args.fix
    
    if dry_run:
        print("=" * 80)
        print("DRY RUN MODE - No changes will be made")
        print("Use --fix to apply changes")
        print("=" * 80)
        print()
    
    if args.fix_unused or args.fix:
        print("Fix: Removing unused keys from all locale files")
        print("-" * 80)
        _, _, unused_keys = validator.check_no_unused_keys()
        if unused_keys:
            actions = fixer.fix_unused_keys(unused_keys, dry_run=dry_run)
            for action in actions:
                print(f"  {action}")
        else:
            print("  No unused keys to remove")
        print()
    
    if args.fix_extra or args.fix:
        print("Fix: Removing extra keys from non-en-US locales")
        print("-" * 80)
        _, _, extra_keys = validator.check_no_extra_keys_in_other_locales()
        if extra_keys:
            actions = fixer.fix_extra_keys(extra_keys, dry_run=dry_run)
            for action in actions:
                print(f"  {action}")
        else:
            print("  No extra keys to remove")
        print()
    
    if args.fix_missing or args.fix:
        print("Fix: Adding missing keys to en-US")
        print("-" * 80)
        actions = fixer.fix_missing_keys_in_reference(dry_run=dry_run)
        for action in actions:
            print(f"  {action}")
        print()
    
    if not dry_run:
        print("=" * 80)
        print("✓ Fixes applied successfully!")
        print("=" * 80)
    
    sys.exit(0)


if __name__ == "__main__":
    main()
