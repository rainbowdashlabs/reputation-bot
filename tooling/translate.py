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
    def __init__(self, code: str, location: Path, entries: dict):
        self.code = code
        self.location = location
        self.entries = entries


def map_lang(lang: str) -> str:
    return lang_mapper.get(lang, lang).replace("_", "-").upper()


def load_properties(path: Path) -> Language:
    lang = map_lang(file.name.replace(f"{prefix}_", "").replace(".properties", ""))
    print("Loading", lang, "from", file)
    with path.open() as f:
        entries = {k: v for k, v in (line.strip().split('=', 1) for line in f.readlines())}
        return Language(lang, file, entries)


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
            print(f"Translating '{escaped}' from {reference.code.upper()} to {target.code.upper()}")
            res = client.translate_text(escaped,
                                        target_lang=map_lang(target.code),
                                        source_lang=reference.code.split("-")[0],
                                        # For some reason "source" doesn't care about variants
                                        formality=Formality.PREFER_LESS,
                                        ignore_tags=["code", "placeholder"],
                                        tag_handling="xml",
                                        model_type=ModelType.PREFER_QUALITY_OPTIMIZED,
                                        preserve_formatting=True
                                        )
            translated = re.sub(r"<code>(.*?)</code>", r"$\1$", res.text)
            translated = re.sub(r"<placeholder>(.*?)</placeholder>", r"%\1%", translated)
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


prefix, reference_lang, path = sys.argv[1:]
reference_lang = map_lang(reference_lang)

langs = dict()

for file in Path(path).rglob(f"{prefix}_*.properties"):
    lang = load_properties(file)
    langs[lang.code] = lang

reference_lang = langs[reference_lang]

for lang in langs.values():
    if lang.code == reference_lang.code:
        continue
    translate_missing(lang, reference_lang)
    write_properties(lang)
