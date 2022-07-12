import sys
import threading
import time
import requests
import json
import base64


API_TOKEN = ''
HEADERS = {'Authorization': 'token ' + API_TOKEN}
#
# Debug login token:
# login = requests.get('https://api.github.com/user', headers=HEADERS)
# print(login.json())


DUMP_FOLDER = 'python3_dump/'
LANG = 'Python3'
FILE_EXTENSION = '.py'
NUM_THREADS = 100

# Note: only the first 1000 search results are available.
PER_PAGE = 100
PAGES = 10

# Wether the process should query GitHub about new repo rankings or load these from file.
QUERY_REPOS = True
# Wether the process should query GitHub about all files in all repos or load these from file.
QUERY_FILES = True

TOO_MANY_REQUESTS = '429: Too Many Requests'


def get_json(r):
    return requests.get(r, headers=HEADERS).json()


def raw_file_at(repo, filepath):
    # This process naturally returns 'TOO_MANY_REQUESTS' when this is true.
    return requests.get('https://raw.githubusercontent.com/' + repo + '/master/' + filepath, headers=HEADERS).text


def raw_file_api_at(url):
    jo = get_json(url)
    if jo['message'].startswith('API rate limit exceeded'):
        return TOO_MANY_REQUESTS
    else:
        return base64.b64decode(['content']).decode('utf-8', 'ignore')


def mine_next_file(files, file_sources, skipped_files):
    finished = False
    while not finished:
        try:
            f = files.pop(0)
            try:
                # file_content = raw_file_api_at(f['url'])
                file_content = raw_file_at(f['repo'], f['path'])
                if file_content == TOO_MANY_REQUESTS:
                    print(file_content)
                    exit(429)
                file_sources.append({'file': f, 'source': file_content})
            except Exception as e:
                skipped_files.append({'file': f, 'error': str(e)})
        except:
            finished = True


def main():
    # Collect most popular repositories repositories.
    #
    repo_rank = []
    #
    if QUERY_REPOS:
        for p in range(PAGES):
            if len(repo_rank) >= 1000:
                break
            #
            try:
                r = get_json('https://api.github.com/search/repositories?q=language:' + str(LANG) + '&sort=stars&order=desc&per_page=' + str(PER_PAGE) + '&page=' + str(p))
                repo_rank.extend(r['items'])
            except Exception as e:
                print('\r', 'Error at page', p, e, end='')

            with open(DUMP_FOLDER + 'repos.json', 'w') as f:
                json.dump(repo_rank, f)
    else:
        with open(DUMP_FOLDER + 'repos.json') as f:
            repo_rank = json.load(f)

    print('Repos found: ', len(repo_rank))

    # Collect source files coordinates from collected repositories.
    #
    files = []
    skipped_repos = []
    #
    if QUERY_FILES:
        for repo in repo_rank:
            try:
                cnts_json = get_json('https://api.github.com/repos/' + repo['full_name'] + '/git/trees/master?recursive=1')
                for f in cnts_json['tree']:
                    if f['type'] == 'blob' and f['path'].endswith(FILE_EXTENSION):
                        files.append({'repo': repo['full_name'], 'path': f['path'], 'url': f['url']})
            except Exception as e:
                skipped_repos.append({'repo': repo['full_name'], 'error': str(e)})

        with open(DUMP_FOLDER + 'files.json', 'w') as f:
            json.dump(files, f)

        with open(DUMP_FOLDER + 'skipped_repos.json', 'w') as f:
            json.dump(skipped_repos, f)
    else:
        with open(DUMP_FOLDER + 'files.json') as f:
            files = json.load(f)

        with open(DUMP_FOLDER + 'skipped_repos.json') as f:
            skipped_repos = json.load(f)
    #
    print('Fles count:', len(files))
    print('Skipped repos:', len(skipped_repos))

    # Mine repository files:
    #
    file_sources = []
    skipped_files = []
    #
    tot_files_to_mine = len(files)
    #
    # threads = []
    for _ in range(NUM_THREADS):
        x = threading.Thread(target=mine_next_file, args=(files, file_sources, skipped_files))
        x.start()
        # threads.append(x)
    #
    #
    FILE_SOURCES_PATH = DUMP_FOLDER + 'file_sources.json'
    SKIPPED_FILES_PATH = DUMP_FOLDER + 'skipped_files.json'
    #
    success_counter = 0
    failure_counter = 0
    with open(FILE_SOURCES_PATH, 'w') as file_sources_file, open(SKIPPED_FILES_PATH, 'w') as skipped_files_file:
        file_sources_file.write('[')
        skipped_files_file.write('[')
        #
        while len(files) > 0 or len(file_sources) > 0 or len(skipped_files) > 0:
            print('\r', 'Completed:', '%.2f' % ((success_counter + failure_counter) * 100 / tot_files_to_mine) + '%', 'Successful:', success_counter, 'Failed:', failure_counter, end='')
            #
            if len(file_sources) > 0:
                file_sources_file.write(json.dumps(file_sources.pop(0)))
                file_sources_file.write(',')
                success_counter += 1
            if len(skipped_files) > 0:
                skipped_files_file.write(json.dumps(skipped_files.pop(0)))
                skipped_files_file.write(',')
                failure_counter += 1

        file_sources_file.write(']')
        skipped_files_file.write(']')


if __name__ == '__main__':
    main()
