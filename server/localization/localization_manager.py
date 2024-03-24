import os
from xml.etree import ElementTree

from constants import DEFAULT_RESPONSE_LANGUAGE


class LocalizationManager:
    def __init__(self, translation_folder):
        self.translations = {}
        self.default_language = DEFAULT_RESPONSE_LANGUAGE
        self.load_all_translations(translation_folder)

    def load_all_translations(self, folder):
        for filename in os.listdir(folder):
            if filename.endswith('.xml'):
                language = filename.split('.')[0]
                self.translations[language] = self.load_translations(os.path.join(folder, filename))

    def load_translations(self, file_path):
        translations = {}
        tree = ElementTree.parse(file_path)
        root = tree.getroot()

        for system in root:
            for category in system:
                for string in category.findall('string'):
                    key = string.get('key')
                    text = string.text
                    translations[key] = text
        return translations

    def get(self, key, language):
        if language not in self.translations and self.default_language in self.translations:
            language = self.default_language
        return self.translations.get(language, {}).get(key, f'[{key} not found]')

    def is_language_supported(self, language):
        return language in self.translations

    def validate_language(self, language):
        if self.is_language_supported(language):
            return language
        return self.default_language
