**Customer Meeting Transcript**

[00:00]
Customer: So, it's recording... I, `[redacted]`, agree to the publication of this transcription and audio recording in a public repository on GitHub and GitLab. Let's begin.

[00:20]
Customer: So, the mock backend is ready. The frontend has already developed the application well with this mock backend, which is awesome. Let's start with the backend. What are we doing this week? Actually, let's discuss the user stories first to see what we want to implement this time. Let's go step by step. First is probably registration.

[00:46]
Team Member 1: Actually, let's discuss the user stories first to see what we want to implement this time. Let's go step by step. Our first user story is exactly that—registration.

[00:49]
Customer: Great. So basically, right now we are doing registration and authorization. What about the frontend? 

[01:14]
Customer: So, let's say we do the registration and authorization user story.

[01:38]
Team Member 3: Look, authorization is basically working, but it currently uses a demo password...

[01:46]
Team Member 1: No, it's not. Right now we just hardcoded all the data. Our user is just a machine. We don't have real registration; from the frontend side everything is built, but we don't verify anything on the backend. We only have one test user and a couple of test passwords.

[02:05]
Customer: I see. We'll get to the backend shortly. Right now I'm asking about the frontend. So your requests are already sending something. A question about authorization: how will we keep the user logged in? With a token? How does the app know they have logged in before?

[02:30]
Team Member 3: Most likely a token stored in memory.

[02:47]
Customer: Right. So regarding authorization: the user opens the app and just sees "Hello" and their name, without having to enter their login and password every time. This will be MVP V1. Again, this is not a strict must-have for the upcoming week, so don't stress if you can't finish it. 

[03:30]
Team Member 1: I had a question: what specific fields do we need for registration? Just login and password? We don't need email verification, right?

[03:45]
Customer: No, no email verification, that's too much hassle. Just login and password. Just make sure not to send the password as raw text to the backend. Hash it.

[04:00]
Team Member 1: We are sending it raw for now.

[04:03]
Customer: For now? That's fine. So, we discussed the frontend. Now, the backend. You need to figure out how to return data to an authorized user. MVP V1 includes registration and displaying this data.

[04:41]
Team Member 1: Will we be adding a vehicle? That's our second user story. Adding a vehicle includes the brand, model, and year. We agreed that one user has one vehicle, right? We need two screens? First the user registers, then the vehicle?

[05:19]
Customer: Yes.

[05:25]
Customer: Can you do vehicle registration by Sunday?

[05:30]
Team Member 3: We can.

[05:37]
Customer: Okay, good. Let's do full registration, including the vehicle.

[05:41]
Team Member 1: When entering the vehicle model, is text input enough, or do we need a dropdown list of models?

[05:50]
Customer: Text input is enough for now. A dropdown would require a massive database of vehicle brands and models. Just typing it manually is fine. 

[08:00]
Customer: What else? Next week, I want AI integration. At least a basic version.

[08:06]
Team Member 1: Is a basic parser enough for the AI? So it recognizes text like "gas station" and logs the value?

[08:20]
Customer: The goal is to make an AI agent. It should parse and log gas, repairs, and routes, but it also needs to analyze statistics and provide summaries. That is goal number one.

[08:52]
Team Member 1: What if the AI server is down? Should we just display an error?

[09:00]
Customer: We can play around with that.

[09:04]
Team Member 1: If the user sends a text, and the AI finds a field unclear—like asking "kilometers or miles?"—should we implement these clarifying questions?

[09:56]
Customer: Good question. Yes, let's include clarifications so it feels like a continuous dialog, rather than starting a new session every time. 

[10:30]
Team Member 1: If the AI interprets the user's input incorrectly, should there be an option to edit it in the timeline?

[10:38]
Customer: Here is what I would ideally like to see: I write "I drove 3 kilometers and spent this much gas." The app redirects me to the trip form, and everything is pre-filled. All I have to do is click "Submit."

[11:16]
Team Member 1: And it doesn't reply in the chat?

[11:20]
Customer: That might be too complex for now. Let's make it simpler. The AI replies, "I'm going to log a trip with these details. Is it okay?" If you say "ok," it saves it to the database. That's purely a backend task. 

[11:54]
Customer: But if you can do the redirect to another screen, that would be amazing.

[12:05]
Team Member 1: We can try, but shouldn't there be some kind of chat response anyway?

[12:15]
Customer: I think so. Maybe include a link to the created item in the timeline. Also, eventually, we want to add photo uploads for the vehicle during registration, as well as the achievements system we discussed earlier.

[12:44]
Team Member 1: Is this for MVP V1?

[12:47]
Customer: No, no, just keep it in mind. For MVP V1: registration, authorization, and displaying the user's vehicle. That's it. It just needs to be functional.

[13:15]
Team Member 1: Should we leave the chat feature out of V1?

[13:20]
Customer: Actually, let's include the chat too. Just a basic chat without memory or statistics, so the user can talk to the AI. Just give it a context prompt and implement basic communication to lay the groundwork for next week. 

[14:30]
Customer: Did you already make the chat UI?

[14:35]
Team Member 2: Yes, the chat is ready.

[14:37]
Customer: Great, I forgot about that. So the plan for the week is set.

[14:50]
Team Member 1: Can we open the product backlog? For formalities, we need to show the tasks we plan to complete. We asked `[redacted]` to make a file with our expected tasks by the end of the week.

[15:50]
Team Member 1: Here it says registration and adding a vehicle.

[16:30]
Customer: We've discussed everything anyway. I have no more questions. Good luck. Will you send the final work on Sunday?

[16:50]
Team Member 1: Yes. The only issue is our backend only works on the university Wi-Fi right now.

[17:00]
Customer: The previous team mentioned this. I'll give you a server so it's accessible over the internet. How are you deploying it?

[17:15]
Team Member 1: I was given a VM, and I ran it using a Docker container.

[17:25]
Customer: You built a Docker image? That's impressive. For now, keep using the university VM, but I'll provide a server because I want to test it myself without going to the university. In the meantime, you can just send a video demonstration.

[18:00]
Team Member 1: Yes, a video works. Is that all?

[18:12]
Customer: Yes, I think so. You can stop the recording.