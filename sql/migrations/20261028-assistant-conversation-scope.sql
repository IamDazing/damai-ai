-- Run after Spring AI has created SPRING_AI_CHAT_MEMORY in the damai_ai database.
-- Scoped assistant conversation IDs can be up to 175 characters.
ALTER TABLE SPRING_AI_CHAT_MEMORY MODIFY COLUMN conversation_id VARCHAR(191) NOT NULL;
