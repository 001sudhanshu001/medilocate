# MediLocate

## Description

**MediLocate** is a comprehensive application designed to streamline the process of scheduling doctor appointments and locating healthcare professionals based on user proximity. This project aims to provide a user-friendly interface for finding doctors, booking appointments, and receiving timely notifications. By integrating real-time location data and sophisticated scheduling algorithms, MediLocate enhances the overall healthcare experience for both patients and doctors.

## Problems It Solves

1. **Find Availability of Doctors Based on Specialty**
    - MediLocate allows users to search for doctors by their specialty. The system provides a list of available doctors, ensuring that users can easily find medical professionals who match their specific needs.

2. **Find Doctors Near Your Location**
    - The application features location-based search functionality that helps users find doctors near their current location. Doctors are sorted based on the shortest distance, allowing users to choose the most conveniently located healthcare providers.
   
3. **Book Appointments and Avoid Conflicting Appointments**
   - Users can book appointments with their chosen doctors through the app. The backend system is designed to prevent double-booking.
4. **Reminder Notifications and Driving Directions**
    - To enhance user convenience, the system sends a reminder notification about one hour before the scheduled appointment. This notification includes a link to driving directions to the doctor’s clinic, helping users navigate to their appointment location with ease.

## Features

- **Specialty-Based Doctor Search:** Filter doctors based on their specialty and city.
- **Proximity Search:** Locate doctors near your current location with distance-based sorting.
- **Appointment Booking:** Schedule appointments with instant confirmation.
- **Conflict Prevention:** Ensure no overlapping appointments at a same Slot.
- **Reminder Notifications:** Receive timely reminders with driving directions to the clinic.
- **Automated Slot Scheduling:** Doctors can define a slot configuration once, and the system will automatically create their schedules based on this configuration. This feature simplifies the scheduling process for doctors and ensures that their availability is managed efficiently.

## API Documentation

### Doctor Controller

- **GET** `/api/doctors/{id}`
   - Retrieve details of a specific doctor by ID.

- **PUT** `/api/doctors/{id}`
   - Update details of a specific doctor by ID.

- **POST** `/api/doctors`
   - Create a new doctor entry.

- **GET** `/api/doctors/search?name=Sandeep`
   - Search for doctors based on name (Used for Type Ahead Search).

- **GET** `/api/doctors/search-closest`
   - Find doctors closest to the user's current location.

- **GET** `/api/doctors/search-by-city-and-specialty`
   - Search for doctors based on city and specialty.

- **GET** `/api/doctors/profile`
   - Retrieve the profile information of the currently authenticated doctor.

- **GET** `/api/doctors/all`
   - Retrieve a list of all doctors.

### Slot Controller

- **POST** `/api/slots/update/{id}`
   - Update an existing slot by ID, only about Authorized Doctor.

- **POST** `/api/slots/create`
   - Create a new time slot for a doctor.

- **GET** `/api/slots/doctor/{doctorId}`
   - Retrieve all slots associated with a specific doctor.

- **DELETE** `/api/slots/{slotId}`
   - Delete a specific slot by ID.

### Doctor Slot Configuration Controller

- **POST** `/api/slot-configuration/set`
   - Set up slot configuration for doctors.

- **GET** `/api/slot-configuration`
   - Retrieve the current slot configuration settings.

### Authentication Controller

- **POST** `/api/auth/signup`
   - Register a new user.

- **POST** `/api/auth/signin`
   - Authenticate and sign in a user.

- **POST** `/api/auth/logout`
   - Log out the currently authenticated user.

- **POST** `/api/auth/create-admin`
   - Create a new admin user.

### Appointment Controller

- **POST** `/api/appointments/complete`
   - Mark an appointment as completed.

- **POST** `/api/appointments/book`
   - Book a new appointment. Handles Double Book Problem.

- **GET** `/api/appointments/user`
   - Retrieve all appointments for the currently authenticated user.

- **GET** `/api/appointments/doctor`
   - Retrieve all appointments for the currently authenticated doctor.

- **DELETE** `/api/appointments/cancel`
   - Cancel a specific appointment by ID, only by Authorized User

### Specialization Controller

- **GET** `/api/specializations`
   - Retrieve a list of all available specializations.
