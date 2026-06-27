# Customer review transcript

[00:00] **Interviewer:** Nikita, hello. Today we would like to conduct a UAT of our application "Lamba". Before we begin, is it okay if we record this session? The recording is only needed to verify our assignment and will be sent exclusively to the professors via Moodle.

[00:16] **Customer:** Yes, of course, I give my consent.

[00:18] **Interviewer:** Great. Today we want to ask you to go through a few common user scenarios freely on your own.
The first user scenario is Registration and Adding a Vehicle. Please register and add your car.

[00:33] **Customer:** Do you hash the password? Can I do it? This account already exists.
So, I need to... Wait a second, let me pick another email. It remembered it. Yes, that’s good.
Alright, the mark. Let's say, VAZ 1107. Here we have 2004, the current mileage. Okay, yes, created. Excellent.

[01:33] **Interviewer:** How easy was it to register?

[01:38] **Customer:** Yes, it was easy. All the fields popped up, everything is clear.

[01:40] **Interviewer:** Were there any confusing moments while adding the vehicle?

[01:45] **Customer:** No, not really.

[01:48] **Interviewer:** Great. Then let's move on to the second UAT scenario: Using the AI Assistant.
Now, imagine you want to ask your car a question. Please try to open the AI assistant and ask something about your vehicle.

[02:30] **Customer:** Oh, look, it shows the mark of the car.

[02:37] **Interviewer:** Good. Was it easy to find the AI assistant?

[02:40] **Customer:** Well, the interface is convenient, but I didn't get the information I needed.

[02:46]  **Interviewer:** Okay. Let's move on to the third scenario: Adding records to the history. Please add a new record to the maintenance or expense history. After adding it, you should see the record reflected in the history. Please check the history. Yes, of course.

[03:43] **Customer:** Alright. History.

[03:56] **Interviewer:** Was the process of adding a record clear?

[04:00] **Customer:** Well, yes.

[04:03] **Interviewer:** How convenient was it to add a record this way? Would you like to be able to add a record manually?

[04:08] **Customer:** Yes, I would.

[04:11] **Interviewer:** Good. And the last one, the fourth UAT scenario: Viewing statistics. Now open the statistics section and look at the information about your vehicle. How clear are the statistics? Are there any metrics you would like to add?

[04:32] **Customer:** Well, overall, yes. Yes, there is also a convenient time filter here. Except that the labels are hard to see. Like, I don't really understand what exactly is there. There are expenses... Expenses for refueling, maintenance. Ah, well, overall, yes. Everything seems clear. Like I said, the labels just don't quite fit. That's all.

[inaudible]

[05:30] **Interviewer:** Which tests passed for the vehicle? Which ones are not fully developed and need to be worked on in the next sprint?

[05:40] **Customer:** So, if I remember correctly, registration and authorization work wonderfully. Except for that part where I accidentally tried to log into my old account. Therefore... The second thing is creating a car, right? If I'm not mistaken. Also, yes, absolutely no complaints. Super. Moreover, all the information is further reflected in the app. That’s really good. Yes. Then, about the AI chat—there are some questions, of course. You can tell there is an artificial intelligence behind it, the answers aren't just robotic. But I would like more, so to speak, correct interaction, let's call it that. Well, and the statistics overall, yes. Meaning, it shows the mileage of expenses. No? No, that's all.

[06:36] **Interviewer:** Did we process your feedback from the previous meeting correctly?

[06:41] **Customer:** Well, if I could only remember, I don't really remember at all. What was my feedback from the last meeting? Well, okay, let me try to remember. So, if I'm not mistaken... It seems we agreed that you would specifically work on authorization and registration, which you did. Moreover, you've already done…

[07:05] **Interviewer:** Well, we agreed on the sidebar, statistics, history, and so on.

[07:11] **Customer:** Well, overall, all of that is there now, so yes. Just the AI chat needs improvements. But again, those are refinements. Meaning it exists, and that's already at least what we agreed on during the last meeting.

[07:25] **Interviewer:** We also had Quality Requirements that we need for... Well, not just as a technical... Well, yes, it is the technical part of the app, but specifically how it works. To be precise, we had a Quality Requirement: If a user or the AI chat does not respond when a user submits a request to create an event with an invalid event type, empty description, negative amounts, negative mileage, or a missing/unknown user ID—during normal system operation, the system rejects the event and does not save the new event record for 100% of such invalid requests.

[08:38] **Customer:** Okay, and what do you need from me?

[08:40] **Interviewer:** Well, basically, do you confirm that it does not save the record if you see that something is incorrect?

[08:49] **Customer:** Yes, yes, I confirm.

[08:54] **Interviewer:** The second Quality Requirement: When a user requests a vehicle timeline, under normal operation of the service side with a demo dataset, the endpoint returns a successful response within two seconds.

[09:07] **Customer:** Well, overall, yes. This is a question about the timeline again, right? Yes, everything is okay there.

[009:23] **Interviewer:** Another question: we created tests so that when one of the developers changes the code, they all pass on GitHub on the datasets.

[09:39] **Customer:** Did you write them for both frontend and backend?

[09:45] **Interviewer:** Only for the backend.

[09:48] **Customer:** And everything will be there, right?

[09:56] **Interviewer:** Yes. Are all the codes clear enough for you—registration, vehicle, profile?

[10:02] **Customer:** Yes, yes, clear enough.

[10:06] **Interviewer:** Are our Quality Requirements acceptable to you?

[10:12] **Customer:** Well, regarding exactly what you described earlier... Man, it feels like no. In the sense that I just want something more. It feels like it's not enough. I don't know. Well, do I understand correctly, these are more Quality Requirements? Ah, okay. Well, overall... Well, yes, you mentioned what I remember about negative numbers and empty responses, right? Well, overall, yes, that will do.

[10:48] **Interviewer:** What are the main remaining issues and risks that you see?

[10:55] **Customer:** In the chat. In the sense that I see that we still need to refine the chat handling because right now it's not clear at all, let's put it that way.

[11:11] **Interviewer:** Which backlog tasks do you think should be completed next or further down the line?

[11:20] **Customer:** Well, definitely the chat refinement. That's probably number one to focus on. Then... I looked at the statistics. Well, I scrolled through it. I see that you have everything there, but I want it to have real data already. Right now it's all zeros because I haven't entered anything, obviously. But I want the statistics to feel more "alive", so to speak. And, ah, well, there's also a minor nitpick—I think I even mentioned it recently—I want the main screen to not have those square lines/blocks because it's not entirely clear that clicking them will send a message to the chat. It feels like it would take you to a different part of the interface. In reality, it just inputs a template and sends it to the AI chat. It's not very clear. So, yes.

[12:17] **Interviewer:** Do you foresee that the project increment we made this week is a useful step toward achieving the goal? Is what we did this week useful in terms of achieving the goal of the project itself?

[12:35] **Customer:** Well, overall, yes. That is, if you compare it to what we had last week, what you showed now is a good step forward. So, yes. Well, I don't know. It's hard to speak about the exact value, let's put it this way. Let's say this week was indeed productive in terms of reaching the goal. Well, I think that's all, as far as I understand.

[13:08] **Interviewer:** Does anyone else have any questions? We don't have any either.
