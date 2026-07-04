# Sprint Review Transcript

**Project:** LAMBA  
**Meeting type:** Sprint Review and customer-executed UAT  
**Date:** 04.07.2026  
**Participants / public labels:** Customer, Team member  
**Permission note:** Recording and public publication of this sanitized English transcript were permitted by the customer.  
**Publication note:** This is a public sanitized English transcript. Account details, private identifiers, and sensitive service or credential details are redacted or generalized.

[00:00]

**Team member:** Good evening. We would like to discuss what we selected for this Sprint, what was completed, and what remains unfinished. We fixed the frontend layout, statistics, AI answers about statistics, extra timeline messages, and added a manual form for adding records. We also fixed incorrect statistics calculations and set up filters. What we did not finish in this Sprint is changing the chat flow, recommendations, and recommendation-based notifications. Did you see that your previous comments were taken into account?

[00:52]

**Customer:** From what you showed me, yes, overall it looks like that. I remember we agreed that the frontend should move away from square blocks toward lines. We also discussed the form, and you made it. It looks similar to the user story from last time.

[01:10]

**Team member:** We will review two main items: manually adding to history and the assistant's answers about statistics.

[01:18]

**Customer:** Great, let's do it. Do I need to create an account now, or can I log into the previous one?

[01:23]

**Team member:** You can log in. Everything should have been saved.

[01:26]

**Customer:** I do not remember which account identifier I used. It worked. I will also arrange the recorder so it does not get in the way.

[01:42]

**Team member:** First, please try manually adding to history. Go to the History tab.

[01:48]

**Customer:** Is it "Add record"?

[01:50]

**Team member:** "Add record" adds through the chat. Go to History and use the plus sign there. From there, you can choose what you want to add.

[02:05]

**Customer:** Mileage means how much I drove, right?

[02:11]

**Team member:** Yes, it is the distance for a trip.

[02:15]

**Customer:** Okay. I can edit it too. For refueling, for example, the mileage was 92 and became 102. Amount, let's say 50. Do I need a decimal point for liters?

[02:30]

**Team member:** No, you can't.

[02:32]

**Customer:** I sometimes refuel an uneven number of liters because a full tank is almost always uneven.

[02:40]

**Team member:** That is fine.

[02:42]

**Customer:** Good. I really like this. The only thing is that it is not immediately intuitive that I need to go to History and press the plus sign, but I understand that, as we agreed, this is a secondary way of adding records.

[03:15]

**Team member:** So the first user story is verified. You can also go to Statistics and check that everything updated there too.

[03:25]

**Customer:** Does that mean it added 20 km to this mileage? How does it work?

[03:31]

**Team member:** Pretty much. You can try adding liters through the chat.

[03:36]

**Customer:** Sure. "Add record."

[03:42]

**Team member:** Now you can go to Statistics. It looks like this data may not be transferring from the form. We need to fix that. This is a bug we can log. Next, for AI answers about statistics, for example, click "Show expenses."

[04:08]

**Customer:** I see it. Very good. Recent expenses.

[04:13]

**Team member:** You can see three refuelings of 10 liters each.

[04:18]

**Customer:** Overall, yes. I started to say something about the route, but we are talking about expenses, so this is fine.

[04:35]

**Team member:** Try "Show the latest data."

[04:42]

**Customer:** I asked for expenses and it did not send the total amount, right? Let's see. Latest what?

[04:50]

**Team member:** Data, I think.

[04:53]

**Customer:** This is a bug. It is showing slightly the wrong thing.

[05:00]

**Team member:** When I tested it on my account, it returned the latest timeline messages with numbers. It may depend on the assistant response.

[05:12]

**Customer:** Maybe it is because my account was created earlier. Could an old account not connect correctly with the new version?

[05:22]

**Team member:** No, it connects. Before, it would not answer about statistics at all and would only ask whether to add a record. This is already running through the server.

[05:32]

**Customer:** So we have verified the second story, right?

[05:36]

**Team member:** Yes. For these two stories, are all the fields you wanted there? Should anything be added?

[05:43]

**Customer:** I remember the uneven number of liters. For statistics, the answer should look neater. Also, this needs to be tested in real life. I cannot say for sure yet, but I want mileage recording to be more precise. For example, it would be useful to have the start of a trip and the end of a trip. You log the odometer at the start and the odometer at the end, and the difference is exactly how much you drove. Right now I may not know how many kilometers I just drove and cannot log the exact value.

[06:35]

**Team member:** The total mileage is shown in the car, right?

[06:40]

**Customer:** Yes. The car has an odometer showing the total mileage. You could log a trip start and trip end using odometer readings. It could be one type of mileage record. For example, we go to the gas station, I note the odometer, we refuel, another record is added, then we drive back and I log the end. The timeline would show start, refueling, end. I am saying this because I assume it may be a small task. I do not need a full feature where a trip formally starts yet, but the current mileage entry is inconvenient.

[07:50]

**Team member:** You want it to be more precise. Okay. Repair and maintenance are also there.

[07:56]

**Customer:** Maybe change "Repair" to "Repair/Breakdown." If something breaks, I want to log it. For example, I notice low antifreeze, take a photo, make a record, and check it later. Can you add a photo attachment?

[08:24]

**Team member:** Where exactly do you want to attach it? In the form?

[08:27]

**Customer:** Yes, in the form. There could be an "Attach photo" option.

[08:32]

**Team member:** Just so it appears in the timeline later?

[08:35]

**Customer:** Yes. I can go into the history later and check it. I am asking because you know your time resources. Is this possible right now?

[08:52]

**Team member:** A basic form attachment may be possible.

[08:55]

**Customer:** The backend would also need to save it somewhere.

[09:00]

**Team member:** We can log this as a separate user story. You want the photo only for yourself in the timeline, not for AI analysis, correct?

[09:14]

**Customer:** No AI analysis is required. What exactly is sent to the AI context when a request is made?

[09:26]

**Team member:** A request is parsed as either a history question or an add-record action. Add-record actions go to the timeline. For AI questions, the backend looks at the database and sends the relevant data.

[09:48]

**Customer:** Okay. For photos and repair, no AI analysis is needed. It would be useful to ask, "What breakdowns did I have recently?" and receive a dated list, for example, "You had an antifreeze leak on [redacted date]." I can go to History myself and look at the photo. Also, adding a record currently feels deep because it takes several clicks.

[10:45]

**Team member:** Adding a record is also available through the chat. The form is an additional path.

[10:50]

**Customer:** True. Maybe I am focusing on the wrong thing.

[10:57]

**Team member:** We could add a shortcut to the form in the quick action buttons. We still need to keep "Add record" for sending to chat, but maybe a separate form button could help.

[11:15]

**Customer:** You probably cannot attach a photo through the chat for a breakdown.

[11:23]

**Team member:** Another option is for "Add record" to redirect to the form.

[11:27]

**Customer:** I have a draft idea. If I write that something broke, it should not immediately add the record to the database. Instead, it redirects to an already filled form. Then I quickly check the form and press Submit. That is where I can attach a photo, so we do not need photo attachments in chat now.

[12:12]

**Team member:** So the user writes in chat, the backend parses the message, returns pre-filled data, and the frontend shows the form for explicit confirmation.

[12:37]

**Customer:** Yes. The chat is not reliable enough for confirming exact information. The AI understands the user, returns what it understood, and the user approves it. If something is wrong, the user edits it and submits.

[12:54]

**Customer:** The app also still needs polish. Some shadows feel blocky, and the bell icon does not work yet.

[12:54]

**Team member:** We planned to add recommendations and then notifications about them in the bell.

[13:06]

**Customer:** When clicking some prompts, it redirects to "Tell me about the car."

[13:14]

**Team member:** We have not fixed that yet.

[13:17]

**Customer:** The main thing is to note it down.

[13:43]

**Team member:** Now about architecture. The architecture diagram shows the customer, Android device, Docker Compose, backend container, and PostgreSQL container. All events go through the backend. The frontend does not process data directly; it sends data through the POST API. The backend processes events and stores data in PostgreSQL. Messages are parsed and sent to the AI and database where needed. Chat history is planned to be saved later.

[14:56]

**Customer:** Where is chat history currently saved? Only in server memory?

[15:02]

**Team member:** Vehicle history is saved, but chat history is not saved.

[15:08]

**Customer:** Understood.

[15:12]

**Team member:** The AI response goes back through the backend to the mobile app.

[15:19]

**Team member:** This connects to point about the pre-filled form. There would be another interaction here: the backend processes the message and returns a filled-out version to the frontend, and then the frontend shows the form.

[15:47]

**Customer:** Exactly. The user just needs to approve it. Again, I am curious what you think, because it seems to me that chat alone is not reliable for confirming exact information.

[16:15]

**Team member:** Regarding tests, we discussed the first three quality requirements last time and now have three more points. When a user asks the assistant a question, the backend passes it to the AI with the correct car and history context and sends only relevant events.

[16:49]

**Customer:** That seems to pass.

[16:53]

**Team member:** If the assistant does not know something, it should not make things up. It should say that it does not know. When the user opens statistics, statistics should return correctly: mileage, expenses, records, and liters. As we just found, adding through the form has an issue with statistics.

[17:22]

**Customer:** Otherwise everything seems fine. I entered amount and liters, and it looked good. The only thing needed is decimal liters. Otherwise, it is good.

[17:35]

**Team member:** Kilometers are also currently integers.

[17:41]

**Customer:** The odometer on my car does not have decimals. Keep kilometers as integers for now. Decimal liters are enough.

[18:00]

**Team member:** For product quality, key backend parts remain under automated control. When we send something, we do not manually change it to make the demonstration look perfect. Messages are processed automatically.

[18:22]

**Customer:** It does not feel like someone is manually answering me in the chat. It feels automated.

[18:51]

**Team member:** Let's recap the main gaps: decimal liters, cleaner statistics chat formatting, expenses not showing correctly in one case, odometer start and end readings, rename or extend Repair to Repair/Breakdown, photo attachment in the form, and recent breakdowns as a dated list.

[19:35]

**Customer:** For recent breakdowns, maybe show the last five with dates. Also, do not be afraid to give the LLM enough context. Saving tokens should not be the main priority right now; the main thing is that it works. [redacted service/account usage details]

[20:54]

**Team member:** We will probably work on the chat redirect to the form, recommendations, and notifications. What improvements would you prioritize for the next Sprint?

[21:12]

**Customer:** Definitely fixing decimal fuel liters. That is a must-have. Second, finish the form so the chat can redirect to the form. Those are the priorities.

[21:46]

**Team member:** How high a priority are the recommendations we already discussed?

[21:52]

**Customer:** I do not want to leave them too far behind. I want them to exist. I do not want the app to only produce dry statistics in a strict AI tone. I want a more natural car-related dialogue.

[22:28]

**Team member:** What would show you that the next version is better for the user?

[22:36]

**Customer:** At least fulfilling the agreements we discussed today. I also want to test the APK in a realistic scenario: get in the car, drive to a gas station, refuel, log everything, and then review it. I want this to be tested not only in theory but in a realistic car-use scenario.
