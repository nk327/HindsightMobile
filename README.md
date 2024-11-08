# hindsight_mobile
* Take a screenshot on your android every 2 seconds.
* Run OCR
* Use Objectbox for RAG
* Pass to LLM running locally using llamacpp

# Settings
* `Chat`: go to chat
* `Ingest Screenshots`: runs a manual ingestion of screenshots
  * Add to db
  * OCR
  * Embed
* `Screen Recording`: Start Screen recording Background Process (May have to click stop on Notification to stop)
* `Manage Recordings`: Takes you to manage recordings screen
  * If checked the app will be record
  * Delete all content (screenshots, videos, embeddings, OCR Results) for a given app (Not implemented yet)
* IMPORTANT PLEASE READ THIS `Record New Apps By Default`: when you enter an app that has not been
    recorded yet it will automatically start recording
* `Auto Ingest`: Auto Run Ingestion at 
* `Hour to Auto Ingest (Military Time)`
  * Default is 2 am
