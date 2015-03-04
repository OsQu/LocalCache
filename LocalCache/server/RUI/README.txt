1. url_list.txt stores all the links that are needed to be cached.
2. all_task.c fetches all urls from url_list.txt, downloads contents from this link and store it into redis database(via stoer_content.sh).
3. sub_task.c parse the image urls, download it and store it into redis database (via store_content.sh).
4. store_content.sh is a shell script used to compress the content and store it to the redis database.
