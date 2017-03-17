State Left - A website designed to help find the nearest competitive state legistlative seats. Currently a program that allows the user to print results to console. Currently in the coding phase.

How to use the example:
Download the Github files. Once this is done, you will need to make a few alterations to the code in order to use the example.

    Setting up File paths
    1) In the Main class, you'll need to make a few changes to the "main" method, specifically you'll need to change the path which the prepareFile and addDataToDatabase methods are aimed at. Aim prepareFile at the sample_file_file_path and aim
    addDataToDatabase at sample_prepared_file_file_path. The file paths are listed below. Copy and paste the necessary ones into the line.
    2) Once this is done, you will need to change where the data from sample_file.txt is sent to. In the TextFileModel class, search for the prepareFile method and change the filePath of the PrintWriter to the addDataToDatabaseFilePath from above. If that sounds complicated, just go to the TextFileModel and search for "data/prepared_data/prepared_file.txt". This is the line you want to replace with the sample_prepare_file_file_path from below.
    3) You will need to change where the keys are aimed at. In the DataParserController class, find the getKeys method. Change the filePath from 'keys/databaselogin.txt' to the appropriate one from below (you can also search for it as well to find it quicker).
    4) Add your login and password to sample_keys.txt. The program uses comma separated values turned into key-value pairs, with "login" and "password" being the keys. Simply add your chosen login and password behind the comma on each line.
    5) Relax! The hard part is over.

    Adding data to the database
    6) In the Main class, uncomment the prepareFile method in the main method (this should be the second line in the main method).
    7) Run the program. If this is successful, your console should say 'File prepared successfully.'
    8) Comment out the prepareFile method, and uncomment the addDataToDatabase line.
    9) Run the program. If it works, it should print several lines to console, the include "Data added successfully." and "Database successfully closed." (The last line printed).
    10) Congratulations! You have now loaded data into the database.

    Run the program!
    11) Almost done! Back in the Main method, comment out the addDataToDatabase line and uncomment the runProgram line (this should be the only line in the main method that is uncommented at this point).
    12) Run the program! With this working, you can now enter your zip code and find the nearest competitive (Hawaiian) state legislative seat!

Windows sample_file_file_path = sample_file\\sample_file.txt
Other sample_file_file_path = sample_file/sample_file.txt

Windows sample_prepared_file_file_path = sample_files\\sample_prepared_file.txt
Other sample_prepared_file_file_path = sample_files/sample_prepared_file.txt

Windows sample_keys_file_path = sample_files\\sample_keys.txt
Other sample_keysFilePath = sample_files/sample_keys.txt