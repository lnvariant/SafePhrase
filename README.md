# SafePhrase #

SafePhrase is an app that triggers certain phone related actions in response to phrase said by a user. The app launches a PhraseService
which continously listens for phrases the user wants. When a phrase is said by the user, the app triggers one of the following actions:

- Take a picture with the front or back camera
- Take a video
- Record audio
- Make a call to a specific number

The actions are done in the background, without any notification to the user (this can be changed in the settings). The phrases that are
said must be unique, and each word must sound sufficiently different than the rest. An internet connection is not required for the app to
function.

The main focus of the app is to be used in emergency or cautionary situations, where the user does not want to make the use of his phone
explicit.

## Dependencies 

[pocketsphinx android](https://github.com/cmusphinx/pocketsphinx) library is used for voice recognition. There have been some small modifications to the source code of pocketsphinx to suit the needs of the app.

