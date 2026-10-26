# B3: Jetpack Components and Libraries

Prepared by: Manar (ManarAlanzi0)

Jetpack Libraries and Technologies

1. Jetpack Compose

Purpose: Builds TravelMate screens using Kotlin composable functions.

Usage in TravelMate: Provides reusable UI components for destination cards, trip forms, and itinerary screens, helping maintain a consistent interface.

2. Material 3

Purpose: Provides UI components such as buttons, cards, and text fields.

Usage in TravelMate: Supports a consistent theme using the colors, typography, and shapes defined in the project’s style guide.

3. Navigation Compose

Purpose: Manages navigation between application screens.

Usage in TravelMate: Handles navigation between login, destination details, trip creation, itinerary, and saved trips screens. It also supports passing destination IDs and trip IDs between screens.

4. ViewModel

Purpose: Manages screen state and user actions.

Usage in TravelMate: Handles actions such as adding or removing activities and separates screen logic from UI components.

5. Kotlin Coroutines

Purpose: Performs asynchronous operations.

Usage in TravelMate: Loads and saves trip data without blocking user interaction, helping keep the application responsive.

6. StateFlow

Purpose: Exposes updated screen state to the UI.

Usage in TravelMate: Updates the itinerary and cost summary when activities are added or removed.

7. Room

Purpose: Stores data locally on the device.

Usage in TravelMate: Stores saved trips, daily activities, and favorite destinations so users can access locally stored travel plans after reopening the application.

8. Firebase Authentication

Purpose: Provides user authentication.

Usage in TravelMate: Supports account registration and login without requiring the team to build its own password-authentication backend.

9. Google Maps Compose

Purpose: Integrates Google Maps into Jetpack Compose interfaces.

Usage in TravelMate: Displays interactive maps and location markers so users can view the locations of attractions and planned activities
