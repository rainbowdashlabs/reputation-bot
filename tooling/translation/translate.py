import json
import os
import re
import sys
from pathlib import Path

import deepl
from deepl import Formality, ModelType

client = deepl.DeepLClient(os.getenv('DEEPL_API_KEY'))

with Path(__file__).parent.joinpath('lang_mapper.json').open("r") as f:
    lang_mapper: dict = json.loads(f.read())


class Language:
    def __init__(self, code: str, location: Path, entries: dict, is_json: bool = False):
        self.code = code
        self.location = location
        self.entries = entries
        self.is_json = is_json


def map_lang(lang: str) -> str:
    return lang_mapper.get(lang, lang).replace("_", "-").upper()


def flatten_json(data: dict, parent_key: str = '', sep: str = '.') -> dict:
    """Flatten nested JSON structure into dot-notation keys."""
    items = []
    for k, v in data.items():
        new_key = f"{parent_key}{sep}{k}" if parent_key else k
        if isinstance(v, dict):
            items.extend(flatten_json(v, new_key, sep=sep).items())
        else:
            items.append((new_key, v))
    return dict(items)


def unflatten_json(data: dict, sep: str = '.') -> dict:
    """Unflatten dot-notation keys back into nested JSON structure."""
    result = {}
    for key, value in data.items():
        parts = key.split(sep)
        d = result
        for part in parts[:-1]:
            if part not in d:
                d[part] = {}
            d = d[part]
        d[parts[-1]] = value
    return result


def load_json(path: Path) -> Language:
    """Load JSON locale file and flatten it for translation."""
    lang = path.stem  # e.g., 'en-US', 'de', 'es-ES'
    print("Loading JSON", lang, "from", path)
    with path.open(encoding='utf-8') as f:
        data = json.load(f)
        entries = flatten_json(data)
        return Language(map_lang(lang), path, entries, is_json=True)


def load_properties(path: Path, prefix: str) -> Language:
    lang = map_lang(path.name.replace(f"{prefix}_", "").replace(".properties", ""))
    print("Loading", lang, "from", path)
    with path.open() as f:
        entries = {k: v for k, v in (line.strip().split('=', 1) for line in f.readlines())}
        return Language(lang, path, entries)


def translate_missing(target: Language, reference: Language):
    key: str
    value: str
    for key, value in reference.entries.items():
        orig = reference.entries[key]
        if re.fullmatch(r"\$[a-zA-Z.]+?\$", orig):
            print("Skipping translation for reference in", key, "@", target.code, ":", orig)
            target.entries[key] = orig
            continue


        if key not in target.entries or not target.entries[key]:
            print(f"Missing translation for {key}@{target.code}. Translating from {reference.code.upper()}")

            # Escape lang code references
            escaped = orig.replace("&", "&amp;")
            escaped = escaped.replace("<", "&lt;")
            escaped = escaped.replace(">", "&gt;")
            escaped = re.sub(r"\$([a-zA-Z.]+)\$", r'<code>\1</code>', escaped)
            escaped = re.sub(r"%([a-zA-Z.]+)%", r'<placeholder>\1</placeholder>', escaped)
            escaped = re.sub(r"\{([a-zA-Z0-9_]+)\}", r'<variable>\1</variable>', escaped)
            print(f"Translating '{escaped}' from {reference.code.upper()} to {target.code.upper()}")
            res = client.translate_text(escaped,
                                        target_lang=map_lang(target.code),
                                        source_lang=reference.code.split("-")[0],
                                        # For some reason "source" doesn't care about variants
                                        formality=Formality.PREFER_LESS,
                                        ignore_tags=["code", "placeholder", "variable"],
                                        tag_handling="xml",
                                        model_type=ModelType.PREFER_QUALITY_OPTIMIZED,
                                        preserve_formatting=True
                                        )
            translated = re.sub(r"<code>(.*?)</code>", r"$\1$", res.text)
            translated = re.sub(r"<placeholder>(.*?)</placeholder>", r"%\1%", translated)
            translated = re.sub(r"<variable>(.*?)</variable>", r"{\1}", translated)
            translated = translated.replace("&amp;", "&")
            translated = translated.replace("&lt;", "<")
            translated = translated.replace("&gt;", ">")
            print(orig, " -> ", translated)
            target.entries[key] = translated


def write_properties(lang: Language):
    with lang.location.open("w") as f:
        dump = dict(sorted(lang.entries.items()))
        for key, value in dump.items():
            f.write(f"{key}={value}\n")


def write_json(lang: Language):
    """Write flattened entries back to JSON file with proper structure."""
    with lang.location.open("w", encoding='utf-8') as f:
        unflattened = unflatten_json(lang.entries)
        json.dump(unflattened, f, ensure_ascii=False, indent=2)
        f.write('\n')  # Add trailing newline


def write_language(lang: Language):
    """Write language file in appropriate format."""
    if lang.is_json:
        write_json(lang)
    else:
        write_properties(lang)


if len(sys.argv) == 4:
    prefix, reference_lang, path = sys.argv[1:]
    reference_lang = map_lang(reference_lang)
    
    langs = dict()
    
    for file in Path(path).rglob(f"{prefix}_*.properties"):
        lang = load_properties(file, prefix)
        langs[lang.code] = lang
    
    reference_lang = langs[reference_lang]
    
    for lang in langs.values():
        if lang.code == reference_lang.code:
            continue
        translate_missing(lang, reference_lang)
        write_language(lang)

elif len(sys.argv) == 3:
    reference_lang, path = sys.argv[1:]
    reference_lang_mapped = map_lang(reference_lang)
    
    langs = dict()
    
    for file in Path(path).glob("*.json"):
        lang = load_json(file)
        langs[lang.code] = lang
    
    if reference_lang_mapped not in langs:
        print(f"Error: Reference language {reference_lang} (mapped to {reference_lang_mapped}) not found in {path}")
        sys.exit(1)
    
    reference_lang = langs[reference_lang_mapped]
    
    for lang in langs.values():
        if lang.code == reference_lang.code:
            continue
        translate_missing(lang, reference_lang)
        write_language(lang)

else:
    print("Usage:")
    print("  Properties mode: translate.py <prefix> <reference_lang> <path>")
    print("  JSON mode: translate.py <reference_lang> <path>")
    sys.exit(1)
