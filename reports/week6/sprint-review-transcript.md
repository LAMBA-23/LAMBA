# Week 6 Sprint Review, Customer Trial, and Transition-Readiness Meeting Transcript

**Status:** Sanitized English transcript.  
**Meeting date:** 2026-07-10
**Participants:** Team representative; Customer.  
**Recording consent:** Granted at the beginning of the meeting.  

[00:00]

**Team representative:** Do you consent to the recording of our conversation?

[00:03]

**Customer:** Yes, I consent to the recording of our conversation.

[00:06]

**Team representative:** Our goal for this Sprint was to finish the main parts of the application. We will now review what has been completed and what remains unfinished.

Events added through the history form are now included in Statistics. We fixed the frontend layout so that content is no longer clipped and the controls are visible. Recommendations in the chat have not been implemented.

The application now stores the five most recent chats locally on the device rather than in the backend database. The local chat history is cleared when the user logs out.

[00:54]

**Customer:** That sounds reasonable.

[00:57]

**Team representative:** The customer previously disliked the formatting of the AI responses about statistics. These responses have now been improved and can be checked during the user acceptance tests. The AI can provide analysis for a week, a month, or another requested period.

The following items are still incomplete: fractional fuel volume, trips entered through starting and ending odometer values, and attaching photos to forms. Voice input was removed from the frontend. We added the application icon.

The authenticated session is now preserved when the application is closed and reopened, so the user does not need to sign in again. For security, passwords are hashed, the demo account was removed, and request-rate protections were added against abusive traffic and brute-force attempts.

The frontend was adapted to different screen sizes. Logout was implemented, including clearing the locally stored chat history. The product is deployed on the server provided by the customer, is accessible outside the university network, and stores the application data there.

[02:26]

**Team representative:** Where would you like to use the application? Are you currently using it? You are not using it yet because we have not formally handed it over, but how would you use it after delivery?

[02:37]

**Customer:** I would use it in everyday life as an assistant. I do not currently use a navigation application regularly, so I could use this application to record trips and refuellings. It would be a form of bookkeeping for the car combined with the AI-based conversation with the car.

[03:00]

**Team representative:** Before the user acceptance tests, I want to clarify what absolutely must be finished. The remaining items include fractional fuel volume, odometer-based trips, repairs and breakdowns, photos, and recommendations. Are all of these mandatory? Recommendations are the only item for which the implementation approach is still unclear; the other items are planned for completion.

[03:32]

**Team representative:** We initially assumed that recommendations would appear as proactive chat messages. However, the chat is request-response based: the AI answers questions but does not initiate messages. We could instead implement rule-based recommendations as notifications.

[04:03]

**Customer:** Yes, that would be sufficient. Recommendations in the chat would be excessive.

[04:16]

**Team representative:** Let us move to the user acceptance tests. The application is already signed in to your account. You can check the chat history.

[04:29]

**Customer:** Let us open Statistics.

[04:54]

**Team representative:** Please check the chat history first. You can open an existing chat and continue the conversation.

[05:07]

**Customer:** Yes, it works. [inaudible]

[05:12]

**Team representative:** The chat history?

[05:16]

**Customer:** Yes, I can see the chat history.

[05:20]

**Team representative:** You can try entering a fractional value, although this is not working correctly yet.

[05:25]

**Customer:** The value can be entered, but it is not saved.

[05:28]

**Team representative:** Trips based on starting and ending odometer values are also incomplete. Repair and breakdown entry is not working correctly either. We have already demonstrated session persistence because the application opened the previously authenticated account.

[05:43]

**Customer:** Amounts in rubles also need to support fractional values for kopecks. Fuel prices are not normally whole numbers.

The newly entered items are not loading. Perhaps they have not been saved yet. Let us try through the chat. I will enter that I refuelled 10 litres. The response may still be arriving from the server, or the issue may be related to this being an older account.

[06:30]

**Team representative:** Add the event and check again. Two events were added. The count changed from eight to ten, so Statistics was updated, but the items did not appear correctly in History. This may be related to the older account because the latest version was mainly tested using another account.

[06:51]

**Customer:** Then please fix it. You may clear my account completely if necessary. It would be useful to adapt it to the new version.

[07:05]

**Team representative:** Session persistence can be demonstrated by closing the application and reopening it. It returns to the same signed-in account. The user can also explicitly log out using the logout control.

[07:28]

**Customer:** That essentially covers the user stories.

[07:33]

**Team representative:** Please also check the AI response for a statistics-related question.

[08:20]

**Customer:** Good. Can I add a repair expense? I think the previous value may have been zero.

[08:34]

**Team representative:** Entries with zero amounts are no longer displayed.

[08:38]

**Customer:** Then we could enter a repair expense and test it again.

[08:45]

**Team representative:** I believe the behaviour is clear; we can stop there.

[08:50]

**Customer:** Then that is essentially everything.

[09:00]

**Team representative:** How ready do you think the application is for handover at this moment?

[09:15]

**Customer:** Approximately 80 percent. Fractional values are a must-have because fuel does not cost a whole-number amount. In general, finish the remaining items that you listed and that should be sufficient.

[09:41]

**Team representative:** Let us discuss the handover. The application is not yet ready for full use. It is currently running on the server that you provided. In what format would you like us to hand over the project? Do you want administrative access to the repository?

[10:11]

**Customer:** I would like to receive the whole project as one or more archives. I need the frontend, backend, and all documentation. Repository access is acceptable, but I do not know where everything is hosted, so archives are sufficient. You could provide them on a flash drive or upload them to cloud storage and send me the link.

[11:00]

**Team representative:** You do not need administrator permissions for the repository?

[11:02]

**Customer:** No.

[11:05]

**Team representative:** We currently maintain `docs/customer-handover.md`. It describes what is available, the current deployment state, and the planned work. It also contains instructions for configuring secret files without exposing the actual secrets. This document will be updated while we finish the MVP.

[11:51]

**Customer:** Documentation is mandatory. Assume that I understand nothing about IT and that I may later hand the product to another team. I want the instructions to be as detailed as possible.

[12:15]

**Team representative:** The handover document already has an initial structure and is being updated. The server is already under your control because it belongs to you. You do not require administrator access to GitHub.

Do you want the application to be made publicly available for other people to use, or should it be handed over only to you?

[12:36]

**Customer:** I do not think public access is necessary. It should be handed over only to me.

[12:50]

**Team representative:** Regarding documentation, do you need only instructions for using and deploying the application, or the entire documentation set, including the handover document, Definition of Done, development process, Git workflow, API information, and change history?

[13:36]

**Customer:** I want all of it. It may be useful for a future team to understand how the product was developed.

[13:45]

**Team representative:** That also includes the quality requirements, roadmap, testing documentation, user acceptance tests, user stories, weekly reports, collaboration evidence, and meeting transcripts.

[14:28]

**Customer:** Yes, I need all of the documentation.

[15:00]

**Team representative:** To complete the handover, we need to finish the remaining scope. Do you have any other requests? Given the deadline, we probably do not need to implement a separate account page.

[15:16]

**Customer:** I agree; the account page is not necessary now. One additional request is to improve the AI prompt. The current responses are too dry. Since the concept is a conversation with the car, the answers should sound more natural and conversational.

[15:50]

**Team representative:** That concludes the review. The remaining work will be addressed in Week 7, after which we should be able to provide the final release.

[15:58]

**Customer:** Great. I will be waiting.