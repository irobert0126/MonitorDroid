#!/usr/bin/env python

from __future__ import print_function

from readconfig import Configure


def main(conf='config.conf'):
    file_mapping = {
        'es': ('../es/kibana-template.yml', '../es/kibana.yml'),
        'web': ('../docker-compose-template.yml', '../docker-compose.yml')
    }

    conf = Configure(conf)

    for k in file_mapping:
        template_file = file_mapping[k][0]
        final_file = file_mapping[k][1]
        if k in conf.config_dict:
            all_words = {}
            replace_words = []
            for check_key in conf.config_dict[k]:
                all_words[check_key] = conf.config_dict[k][check_key]
                replace_words.append("%s" % check_key)

            with open(template_file) as f:
                f_write = open(final_file, 'w')
                for l in f:
                    for w in replace_words:
                        r_w = "[%s]" % w.upper()
                        if r_w in l:
                            print("orginal: %s, new: %s" % (l.strip(), l.replace(r_w, all_words[w]).strip()))
                            l = l.replace(r_w, all_words[w])
                    f_write.write(l)


if __name__ == '__main__':
    main()
