# Build_Updater

1.Run test before startup : ./gradlew clean test
(Includes unit and integration test)

2. For start app you can use docker compose file and execute the following command:
   docker-compose up --scale web={any_number}

App includes three jobs, started by timer (schedule can be specified in config):

1. Refresh job, checks if new updates appeared and saves to database new builds
2. Download job, downloads tar files for created builds (uses optimistic locking for build update, to prohibit download
   by few coroutines at the same time)
3. Product info retrieve job, retrieves product-info.json from tar file.   