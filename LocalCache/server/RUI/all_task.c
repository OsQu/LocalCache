#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define MAX_LENGTH_URL 500
#define MAX_LENGTH_CMD 500
int main(void){
	//system("echo 'hello'");
	FILE * urlFile = NULL;
	char url_string[MAX_LENGTH_URL];
	char sys_command[MAX_LENGTH_CMD];	

	urlFile = fopen("url_list.txt", "r");
	if(urlFile == NULL){
		system("echo failed to open url_list.txt >> error.txt");
		return -1;	
	}
	
	while((fgets(url_string, MAX_LENGTH_URL, urlFile)) != NULL){ //get url
		//get content from url
		printf("get content from: %s", &url_string);
		strcpy(sys_command, "curl ");
		strcat(sys_command, url_string);
		
	 	system(sys_command);	
	}

	fclose(urlFile);
	
	return 0;
}
