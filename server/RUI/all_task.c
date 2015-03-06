#include <stdio.h>
#include <stdlib.h>

#define MAX_LENGTH_URL 360
#define MAX_LENGTH_CMD 400

void delete_return(char *string){
	int i=0;
	while(string[i] != '\n'){
		i++;
	}
	string[i] = '\0';
	
}


int main(){
	FILE *fp = NULL;
	fp = fopen("url_list.txt", "r");
	if(fp == NULL){
		printf("fatal: failed to open url_list.txt\n");
		return -1;
	}
	
	char URL[MAX_LENGTH_URL];
	memset(URL,'\0',MAX_LENGTH_URL*sizeof(char));
	while(fgets(URL, MAX_LENGTH_URL, fp) != NULL){
		//printf("%s", &URL);
		delete_return(URL);
		char CMD[MAX_LENGTH_CMD];
		strcpy(CMD, "curl ");
		strcat(CMD, URL);
		strcat(CMD, " > TMP/");
		strcat(CMD, URL);
		strcat(CMD, ".txt");
		//printf("%s\n", &CMD);
		system(CMD);
	}

	fclose(fp);	
	return 0;
}
