# [Shop Floor Assistance](https://github.com/david-todorov/shop-floor-assistance) enhanced with [OData](https://www.odata.org/) developed by [Olingo](https://olingo.apache.org/)


## OData-Shop-Floor-Assistance

This enhancement integrates OData (Open Data Protocol) into the [Shop Floor Assistance](https://github.com/david-todorov/shop-floor-assistance) to enable seamless and standardized data access. OData is an open protocol for building and consuming RESTful APIs, widely recognized for its powerful query capabilities and interoperability with diverse data sources. The implementation uses OLingo to adhere to the OData standard.

## Docker

### Prerequisites

Before running the application, ensure you have the following installed:

- **Docker Engine**: Make sure Docker is installed and running on your machine.
- **Docker Compose**: Docker Compose is also required. It typically comes with Docker Desktop, but you can install it separately if needed.

### Running the Application with Docker Compose

To run the backend and database applications using Docker Compose, follow these steps:

1. **Navigate to the root directory of the project**:
   ```bash
   cd ~/path/to/OData-OLingo-Shop-Floor-Assistance
   ```

2. **Run the Compose command**:
   ```bash
   docker-compose up --build --detach
   ```

### Running Backend or Database Individually

- **Running only the Backend** (will start the database as a dependency):
   ```bash
   docker-compose up --build --detach backend
   ```

- **Running only the Database**:
   ```bash
   docker-compose up --build --detach database
   ```

### Stopping Containers

- **Stopping all containers**:
   ```bash
   docker-compose down
   ```

- **Stopping only the Backend**:
   ```bash
   docker-compose stop backend
   ```

- **Stopping only the Database**:
   ```bash
   docker-compose stop database
   ```

### Environment Variables

- **`.env` file**: An `.env` file, required for creating the database and backend container, is included in the repository as an example.
- **Running Backend Locally**: If you run the backend from an IDE, ensure the database is started first. Identical environment variables should be set to match those in Docker.
- **Local Database URL**: If the backend runs outside a Docker container, make sure the `databaseUrl` points to your local database (e.g., `localhost`).
- **JWT_SECRET_KEY**: The secret key must be an HMAC hash string of 256 bits; otherwise, the token generation will throw an error.

### Database Data Persistence

- The database used in this application is configured to be persistent by utilizing Docker volumes.
- In the `docker-compose.yml` file, a named volume (`postgres_data`) is created and mapped to the PostgreSQL container’s data directory (`/var/lib/postgresql/data`).
- Additionally, the data is stored in a folder named `database` in the repository.
- This setup ensures that all database data remains intact even when the container is stopped or removed, allowing for seamless data management across container lifecycles.

### Ports Used

- **Backend**: Available at [http://localhost:8080](http://localhost:8080)
- **Database**: Available at [http://localhost:5432](http://localhost:5432)

## API Documentation

For detailed API documentation, refer to the following files:
- [API_DESCRIPTION.md](documentation/API_DESCRIPTION.md) for the standard API.
- [ODATA_API_DESCRIPTION.md](documentation/ODATA_API_DESCRIPTION.md) for the extended new OData API.
- [SOFTWARE_DESIGN.md](documentation/SOFTWARE_DESIGN.md) for the software design  and future development.

## Running the Backend

For detailed instructions on running the backend, refer to the [DEPLOYMENT.md](documentation/DEPLOYMENT.md) file.