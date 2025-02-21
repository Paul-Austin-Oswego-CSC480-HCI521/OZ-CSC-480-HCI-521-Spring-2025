===========================================
         PROJECT INSTALLATION GUIDE
===========================================

PREREQUISITES:
--------------
Before you begin, ensure you have the following installed:
- GitHub Desktop (or Git CLI)
- Java 21 (Ensure it matches the OpenLiberty setup)
- Maven (for building the backend)
- OpenLiberty (will be set up with Maven)

===========================================
          INSTALLATION STEPS
===========================================

USING GITHUB DESKTOP:
---------------------
1. Download the project files:
   - Click "Download ZIP" on GitHub.
   - Extract the .zip file to your preferred location.

2. Clone the repository via GitHub Desktop:
   - Open GitHub Desktop.
   - Navigate to "File → Clone Repository".
   - Select "URL" and enter the repository link.
   - Choose a local directory and click "Clone".

===========================================
       BUILDING BACKEND WITH MAVEN
===========================================

Navigate to the backend directory:

    cd csc480-web/_backend

Run the Maven build command:

    mvn clean package

===========================================
      RUNNING QUOTE-SERVICES SERVER
===========================================

Navigate to the services directory:

    cd csc480-web/_backend/services/quote-service

Run the build script:

    ./build.bat

After successful compilation, start the server:

    mvn liberty:run

For development mode, use:

    mvn liberty:dev

Once the server is running,  access the API documentation at:

    http://localhost:9082/

API documentation is included with the service.

===========================================
      RUNNING USER-SERVICES SERVER
===========================================

Navigate to the services directory:

    cd csc480-web/_backend/services/user-service

Run the build script:

    ./build.bat

After successful compilation, start the server:

    mvn liberty:run

For development mode, use:

    mvn liberty:dev

Once the server is running, access the API documentation at:

    http://localhost:9081/

API documentation is included with the service.

===========================================
      TROUBLESHOOTING & SUPPORT
===========================================

- Ensure Java, Maven, and OpenLiberty are installed correctly.
- Check server logs for errors if the API is not accessible.
- For additional help, refer to the project documentation.

===========================================
                 END
===========================================
