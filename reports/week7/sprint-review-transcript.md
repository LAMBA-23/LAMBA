# Week 7 Sprint Review, Customer UAT, and Final Transition Meeting Transcript

**Status:** Sanitized English transcript.  
**Meeting date:** 2026-07-18  
**Participants:** Team representative; Customer.  
**Recording permission:** Granted before recording; the consent statement is not included in the supplied transcript excerpt.  

[00:00]

**Team representative:** At the previous meeting, we identified several issues that left the product approximately 80% ready. Those issues have now been addressed, and several features were added for the final version.

The application now supports decimal values for litres, expenses, and other applicable numeric fields. New history records are shown first. Trips can be recorded using start and end odometer values. Repair and breakdown records were prepared during the previous Sprint.

[00:43]

**Team representative:** Photos can be attached to breakdown records and are displayed without cropping. Recommendations are available on a separate screen. The assistant's responses are more natural and are written from the vehicle's perspective.

The application also includes selectable communication styles, a user profile, password change and logout functions, and vehicle-data export. Vehicle registration now provides a list of common brands and models. Voice input was added, including cleanup of recognised text before it is placed in the message field.

[01:30]

**Customer:** The fact that a photo can already be attached to a breakdown record is excellent. Overall, it looks very good.

[02:00]

**Team representative:** We will now conduct user acceptance testing of the new features using my phone.

[02:18]

**Team representative:** The first scenario is decimal values. Please open a fuel record and enter decimal values for litres and rubles.

[02:28]

**Customer:** I will enter 5.6 litres of 92-octane fuel for 1288.1 rubles. Save.

[02:50]

**Team representative:** Please verify that the decimal values were saved. You can also check them in Statistics.

[02:55]

**Customer:** Yes, I can see them.

[03:02]

**Team representative:** The second scenario is recording a trip by odometer values. Start a trip and enter the current odometer value.

[03:22]

**Customer:** That is reasonable. The current implementation is acceptable. We can finish the trip.

[03:28]

**Customer:** Can I add other records while a trip is active?

[03:30]

**Team representative:** Yes. You can start a trip, add a fuel record, and then finish the trip.

[03:33]

**Customer:** The trip was added successfully.

[03:52]

**Team representative:** We can now test adding a repair or breakdown record.

[03:56]

**Customer:** Does the trip update History and Statistics? I entered a different value, so everything appears to be correct.

[04:05]

**Team representative:** You can attach a photo to a breakdown record.

[04:08]

**Customer:** I will create a breakdown record for the wheels and attach a sample photo.

[04:32]

**Team representative:** Before closing the form, note that the photo display was adjusted so the image fits correctly on different phones.

[04:47]

**Customer:** That looks good. Did the photo load? Let us try another photo. It may not have finished loading yet.

[05:13]

**Team representative:** It has loaded. We only needed to wait.

[05:18]

**Customer:** The result is good.

[05:30]

**Team representative:** Please open the recommendations screen. The red indicator shows that there are unread recommendations.

[05:36]

**Customer:** I can see a recommendation related to the recent breakdown. What other recommendations can the application provide?

[05:44]

**Team representative:** It can recommend an oil change based on mileage or elapsed time.

[05:58]

**Team representative:** You can also test communication with the vehicle using different assistant styles.

[06:05]

**Customer:** The selfish style is currently selected. I will try voice input: "Hi, there is no fuel available, so you will remain in the garage for another week."

[06:38]

**Customer:** It almost recognised the message. I will try again: "Refuelling, 10.5 litres for 700 rubles." What technology is used for recognition?

[06:58]

**Team representative:** A neural-network-based transcription service is used.

[07:06]

**Customer:** The record was saved. Voice input works.

[07:39]

**Customer:** The assistant returned a sarcastic response in the selected selfish style. The style is clearly visible in the response. Good.

[08:03]

**Team representative:** Please open the Profile screen. You can verify the username, select a local profile photo, and change the password.

[08:38]

**Team representative:** You can also export the vehicle data to a spreadsheet.

[08:42]

**Customer:** The export feature is very useful. For example, a vehicle owner could share the history with a repair shop. Can the exported file be sent elsewhere?

[09:00]

**Team representative:** The user selects where to save the file. We also replaced the previous system-style logout confirmation with an application-styled dialog.

[09:21]

**Customer:** I entered the password incorrectly, and the error was handled correctly. The updated interface looks good.

[09:53]

**Team representative:** Let us discuss the final handover. We will update the documentation after the meeting and send the complete archive through Telegram or private cloud storage.

[10:19]

**Customer:** The archive may be large, so a private cloud-storage link would be preferable.

[10:23]

**Team representative:** The backend is already running on the server you control. The final package will also include build, run, and usage instructions. The latest GitHub release will contain the relevant product information.

[10:55]

**Team representative:** We would like final confirmation of the product's readiness. Are you satisfied with the result?

[11:02]

**Customer:** Yes. I do not currently see any significant problems. The previously identified issues with decimal values, icons, and naming appear to be resolved. Overall, I am satisfied with the result.

[11:20]

**Team representative:** Is the final customer-confirmation status `Accepted`?

[11:23]

**Customer:** Yes.

[11:24]

**Team representative:** The customer-controlled server will remain entirely under your control, and the team will no longer manage it after transition. Are you satisfied with this arrangement?

[11:45]

**Customer:** Yes.

[11:46]

**Team representative:** The archive will include the documentation under `docs/`, including `docs/customer-handover.md`, the acceptance status, the reached transition level, and the transfer details.

[13:00]

**Team representative:** We will send the archive after the documentation is updated.

[13:06]

**Customer:** Excellent. We successfully completed.