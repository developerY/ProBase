# Task: Fix State Desync & Glasses Microphone Permissions

## State Synchronization
- [ ] Create `@Singleton` `TranslationRepository` in `:features:xr:glass:translation`
- [ ] Update `TranslationViewModel` to use the shared repository
- [ ] Ensure `TranslationScreen` (glasses) observes the shared repository

## Glasses Permissions Fix
- [ ] Implement `ProjectedPermissionsResultContract` in `UnifiedTranslationScreen.kt`
- [ ] Request `RECORD_AUDIO` for the glasses specifically

## Verification
- [ ] Build modules
- [ ] Verify simultaneous transcription on phone and glasses
- [ ] Verify Error 9 is resolved using projected permissions
