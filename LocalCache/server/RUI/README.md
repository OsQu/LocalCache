1. url_list.txt stores all the links that are needed to be cached.
2. all_task.c fetches all urls from url_list.txt, downloads contents from this link and store it into redis database(via stoer_content.sh).
3. sub_task.c parse the image urls, download it and store it into redis database (via store_content.sh).
4. store_content.sh is a shell script used to compress the content and store it to the redis database.
5. error.txt stores all the error msgs.
6. all downloaded file will be stored into TMP and then compressed and stored into database. After sub_task parsed all the downloaded files, all files in TMP folder shall be deleted.

