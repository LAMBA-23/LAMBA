# Customer Meeting Transcript

[00:00]
**Team Lead:** Everything is recording. Great.

[00:05]
**Customer:** Let me open the notes... Alright. Yes, so this week we were generally supposed to just do some research. Ah, I remember, you were also supposed to look into the architecture—not necessarily prepare it fully, but at least see how it's done.

[00:25]
**Team Lead:** Yes, for the project, we also have user stories and some formal things regarding principles. We did that.

[00:35]
**Team Member 1:** Yes, we needed to prepare them according to a specific template. We can discuss them now.

[00:42]
**Customer:** Let's do that. User stories are actually really important right now; it would be interesting to look at them.

[00:48]
**Team Lead:** The only thing is, we also had... well, the design. And we needed to provide you with an MVP, but we only found out about this yesterday. They only published the assignments yesterday, so...

[01:02]
**Customer:** You need to provide me with an MVP?

[01:05]
**Team Lead:** Yes.

[01:06]
**Customer:** Well, that makes no sense. I mean, I'm not arguing with the requirements of your `[TAs]`. We both understand that you couldn't possibly prepare a real MVP in this short time, so it's fine.

[01:20]
**Team Lead:** Unfortunately, we have to submit it by Sunday.

[01:25]
**Customer:** Oh, wow. Okay, good.

[01:28]
**Team Lead:** It's the very first, most basic version of an MVP.

[01:32]
**Customer:** Well, maybe just make something clickable in Figma and that's it.

[01:36]
**Team Member 1:** That's a separate assignment; we have to do the clickable Figma anyway. 

[01:42]
**Team Lead:** We only got this yesterday. I realized that we definitely wouldn't be in time for this meeting.

[01:48]
**Customer:** It's okay. Let's think. Today is Thursday. So by Sunday evening, you need to prepare an MVP and submit it?

[02:00]
**Team Lead:** We need to upload it to the `[University]` virtual machine so the TA can log in and check it.

[02:08]
**Customer:** Just to see that it exists. Okay. Let's think about how to get you an MVP by Sunday. The maximum plan is definitely out the window. Who is your TA?

[02:25]
**Team Lead:** `[TA Name]`.

[02:27]
**Customer:** Let's assign roles. Who is doing what?

[02:35]
**Team Lead:** `[AI Engineer]` is the AI engineer, I am the Team Lead, `[Designer]` is the designer, `[Backend Dev]` is backend, and `[Frontend Dev]` is frontend.

[02:45]
**Customer:** Got it. Regarding the conditions: will they only look at the VM? Will they even look at the mobile app?

[02:55]
**Team Lead:** We need to provide it in a way that he can see we are working on it.

[03:02]
**Customer:** Okay, I get it. I need a whiteboard. I'll go grab one.
*[Background noise of moving a whiteboard]*

[03:20]
**Customer:** Okay, what do we want to make by Sunday? Frontend, you're making the mobile app, right? Have you done this before? No? That's fine. Let's try the minimum program.

[03:45]
**Team Member:** So that's the profile, registration...

[03:48]
**Customer:** We can even skip registration for now. I mean, we will do it eventually, but right now, if I were the strict customer, I'd want to see how the app actually works. For me, that's not registration; it's the chat. For the MVP, let's make a mobile app that is just a chat sending requests to the backend. The backend will have some basic memory to remember the user and their car, and we just hardcode some facts into text, send it to the Neural Network API with context, and return the answer. That's the baseline to show the concept. Can you manage that in two full days? 

[04:40]
**Team Lead:** We have some design drafts.

[04:45]
**Customer:** Show me.

[04:48]
**AI Engineer:** Here are three options, just different color corrections. We have registration/sign up. Then screens filling in car info (year, mileage, etc.). A tab with the AI—like a digital twin of the car. And our expenses: fuel, parts, etc.

[05:20]
**Customer:** Not bad. Have you formulated how the user interacts with the app? Basically, the user stories.

[05:30]
**Team Lead:** Yes. The user registers, logs in, and adds their car. Then there are a few tabs. One shows the history of additions (fueling, maintenance). Another shows current metrics and statistics. And there's a chat where they can consult the AI or add history.

[05:55]
**Customer:** Question for the AI engineer: how will the AI interact with the car info? Are you planning to use an MCP server or something?

[06:05]
**AI Engineer:** No idea yet.

[06:08]
**Customer:** Let's think together. Let's take a high-level data model. We have a User. The User has info (name, etc.) and `cars` (car IDs). Since we agreed on one car for the MVP, let's leave it at `car`. 
What is a car? It has records: fuel, repair, mileage tracking. Then there is the behavior/AI interaction feature. For the MVP, let's not overcomplicate the database. 

[07:15]
**Backend Dev:** Will we have about four tables? User, Car, Records, and Chat? 

[07:22]
**Customer:** Yes, we need a `chats` and `messages` table to distinguish between AI and user messages.

[07:35]
**Team Lead:** Should the backend verify that the AI returned complete structured data, or do we assume the user provides full info for now?

[07:45]
**Customer:** For MVP v0, skip validation. Let's take the happy path. The client sends a message, the mobile app sends it to the backend (FastAPI/Flask), the backend adds memory context and sends it to an LLM API, and the response goes back to the client.

[08:15]
**Customer:** Frontend, what are your thoughts? Just functional buttons for now. Don't worry about adapting to all screen sizes.

[08:25]
**Frontend Dev:** So an auth screen, car registration screen, and the AI chat screen.

[08:35]
**Team Lead:** We can just make dummy pages for mileage and expenses, and the backend endpoints will return mock data.

[08:45]
**Customer:** Exactly. Just hardcode the return values so the frontend can work. Let me write this down. Frontend: stubs are fine, but the app must actually make a GET/POST request to the backend. Can you deploy it to the `[University]` network?

[09:15]
**Team Lead:** Yes.

[09:20]
**Customer:** Good. Focus on the AI chat. Make it the primary interaction. Don't make the user dig through statistics first to find the chat; it should be chat-first, like ChatGPT, maybe with statistics in a sidebar. 
For Backend: make mock endpoints. Look into using a free API like Mistral for the LLM. 

[10:10]
**Team Lead:** Is Mistral API free?

[10:15]
**Customer:** Yes, and I heard the university has servers for training/renting, but Mistral is easier for now. We also need to approve the User Stories, MoSCoW priorities, and MVP scope. Do you have the repository set up?

[10:45]
**Team Lead:** Yes, we created the repository according to the rules, we have the User Stories, MoSCoW, and the design. We just need to upload the MVP. 

[11:00]
**Customer:** Great. I officially approve the User Stories, the MoSCoW priorities, and this initial MVP v1 scope we just outlined. I also approve the prototype screens. 

[11:15]
**Team Lead:** We need your written consent to use the public MIT-licensed development model for the repository. A Telegram message is enough.

[11:25]
**Customer:** Sure, I will send the confirmation in Telegram right after this. 

[11:35]
**Team Lead:** Also, is it okay if we use Kotlin to build it just for Android for the MVP? iOS will take too much time right now.

[11:45]
**Customer:** Yes, perfectly fine. Don't waste time on cross-platform right now. Focus on making it scalable for the future. And you can use AI tools to help write the code, that's completely allowed.

[12:10]
**Team Lead:** Thank you. We will send you a report in the chat on Sunday.

[12:20]
**Customer:** Excellent. Keep up the dynamic. See you.