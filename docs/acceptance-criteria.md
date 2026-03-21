# User Stories and Acceptance Criteria (BDD)

---

## User Story 1: Register user

As a new user,  
I want to create an account,  
so that I can use the application and save my data.  

**Acceptance criteria (BDD):**

Given I am not logged in  
When I register with a valid email and password  
Then a new user account is created  
And the email is stored as unique  
And I can log in with the created credentials  

Given I provide invalid input  
Then I receive an error message  

---

## User Story 2: Login

As a user,  
I want to log in,  
so that I can access my plants and tasks.  

**Acceptance criteria (BDD):**

Given I am a registered user  
When I provide valid login credentials  
Then I receive a valid authentication token (JWT)  
And I gain access to protected resources  

Given I provide invalid credentials  
Then I receive an error message  

---

## User Story 3: Logout

As a user,  
I want to log out,  
so that my account is protected when I stop using the system.  

**Acceptance criteria (BDD):**

Given I am logged in  
When I choose to log out  
Then my authentication token is removed  
And I no longer have access to protected resources  

---

## User Story 4: Add plant

As a user,  
I want to add a plant,  
so that I can keep track of my plants.  

**Acceptance criteria (BDD):**

Given I am logged in  
And I enter a plant name in the search field  
When I search for a plant  
Then a list of matching plants is retrieved from the external API  

Given I see a list of plants  
When I select a plant from the list  
Then the selected plant is saved in my plant list  
And the plant is associated with my user  
And the plant appears in my plant overview  

---

## User Story 5: View plants

As a user,  
I want to see a list of my plants,  
so that I get an overview of my garden.  

**Acceptance criteria (BDD):**

Given I am logged in  
When I request my plants  
Then I receive a list of my saved plants  
And only my own plants are shown  

Given I have no plants  
Then an empty list is returned  

---

## User Story 6: View sowing plan

As a user,  
I want to see a sowing plan,  
so that I know what to sow and when.  

**Acceptance criteria (BDD):**

Given I am logged in  
When I request my sowing plan  
Then I receive a list of plants sorted by sowing month  

---

## User Story 7: To-do list

As a user,  
I want to manage tasks in a to-do list,  
so that I can keep track of my gardening activities.  

**Acceptance criteria (BDD):**

Given I am logged in  
When I create a task  
Then the task is saved in my to-do list  

Given I have tasks  
When I request my to-do list  
Then I receive a list of my tasks  

Given I delete a task  
Then the task is removed from my to-do list  

---

## User Story 8: Chatbot

As a user,  
I want to ask a chatbot about plants,  
so that I can get guidance.  

**Acceptance criteria (BDD):**

Given I am logged in  
When I ask the chatbot a question about plants  
Then I receive a relevant response  

Given the chatbot cannot answer the question  
Then I receive a fallback response  

---

## User Story 9: Frost warning

As a user,  
I want to receive frost warnings,  
so that I can protect my plants.  

**Acceptance criteria (BDD):**

Given weather data indicates frost conditions  
When frost is detected  
Then a frost warning is generated  

Given I am a user  
When frost conditions are met  
Then I receive a frost warning notification  

---

## User Story 10: Admin overview

As an admin,  
I want to see statistics about users and plants,  
so that I can gain insight into system usage.  

**Acceptance criteria (BDD):**

Given I am an admin  
When I access the admin dashboard  
Then I can see total number of users  

When I view statistics  
Then I can see top 10 most selected plants  

When I view recent activity  
Then I can see the latest user actions  

---

## User Story 11: Notifications

As an admin,  
I want to send notifications to users,  
so that I can remind them about relevant gardening tasks.  

**Acceptance criteria (BDD):**

Given I am an admin  
When I create a notification with a scheduled date  
Then the notification is stored  

Given the scheduled date is reached  
When the system processes notifications  
Then the notification is sent to users  

Given I am a user  
When I receive a notification  
Then I can view the message  

---
