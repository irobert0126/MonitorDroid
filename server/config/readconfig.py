#!/usr/bin/env python
# __author__ = 'Zhaoyan Xu'

from __future__ import print_function
import logging

import collections
import yaml

logger = logging.getLogger()


def represent_dictionary_order(self, dict_data):
    return self.represent_mapping('tag:yaml.org,2002:map', dict_data.items())


def setup_yaml():
    yaml.add_representer(collections.OrderedDict, represent_dictionary_order)


class Configure:

    def __init__(self, filename):
        """
        get_config function applied in python2.7
        :param filename: configuration filename
        :return: 
        """

        import ConfigParser

        self.config = ConfigParser.ConfigParser()
        self.config.read(filename)
        self.config_dict = {}

        for s in self.config.sections():
            self.config_dict[s] = self.config_section_map(s)

        self.services = collections.OrderedDict()

    def config_section_map(self, section):
        dict_sections = {}
        options = self.config.options(section)
        for option in options:
            try:
                dict_sections[option] = self.config.get(section, option)
                if dict_sections[option] == -1:
                    logger.warning("skip: %s" % option)
            except Exception as e:
                logger.error("exception on %s!" % option)
                dict_sections[option] = None

        return dict_sections

    def generate_image_compose(self, template_file='dockerfile_template.yml', out_file='docker-compose.yml'):
        self.read_to_yaml(template_file)
        for s in ['web', 'es', 'db']:
            envs = self.services['services'][s]['environment']
            tmp = {}
            for env in envs:
                k, v = env.split('=', 1)
                tmp[k] = v

            for k in tmp:
                if k.lower() in self.config_dict[s]:
                    tmp[k] = self.config_dict[s][k.lower()]

            envs = ['%s=%s' % (k.upper(), tmp[k]) for k in tmp]
            self.services['services'][s]['environment'] = envs

        self.write_to_yaml(out_file)

    def get_value(self, section, key):
        if section in self.config_dict and key in self.config_dict[section]:
            return self.config_dict[section][key]

        return None

    def write_to_yaml(self, filename='docker-compose.yml'):
        with open(filename, 'w') as yaml_file:
            setup_yaml()
            yaml.dump(self.services, yaml_file, default_flow_style=False)

    def read_to_yaml(self, filename='dockerfile_template.yml'):
        setup_yaml()

        tmp = collections.OrderedDict()

        with open(filename, 'r') as yaml_file:
            for key, value in yaml.load(yaml_file).iteritems():
                tmp[key] = value

        key_list = ['version', 'services', 'volumes', 'networks']
        for k in key_list:
            self.services[k] = tmp.get(k, None)

        print(self.services)


if __name__ == '__main__':
    config_instance = Configure('config.conf')
    config_instance.generate_image_compose()


