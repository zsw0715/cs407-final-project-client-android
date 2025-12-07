# Knot — Android Client  
## Checkpoint Progress Report

## 1. Weekly Roles

| Week        | Coordinator | Observer |
|-------------|-------------|-----------|
| **Week 10** | Shenwei Zhang | Xiangyi Peng |
| **Week 11** | Liye Fu | Enhui Zhao |
| **Week 12** | Shenwei Zhang | Xiangyi Peng |
| **Week 13** | Xiangyi Peng | Enhui Zhao |
| **Week 14** | Enhui Zhao | Shenwei Zhang |

---

## 2. Work Summary — Week 10

### Implemented Features
- **Map and Comment UI — Xiangyi Peng**  
  - Basic map interface  
  - Comment display & creation prototype  

- **Chat and Settings UI — Liye Fu**  
  - Chat page basic structure  
  - Settings page components  

- **Backend Core Implementation — Shenwei Zhang**  
  - User/session backend setup  
  - Initial friend & profile backend endpoints  
  - Supported basic data models for the app  

- **Login & Signup UI — Enhui Zhao**  
  - Completed initial login and register pages  
  - Connected navigation flow  

### Deliverables
- Completed PPT summarizing UI flow, backend architecture, and feature overview.

---

## 3. Work Summary — Week 11

### Implemented Features
- **Chat System Enhancements — Liye Fu**  
  - Updated chat interface  
  - Prepared structure for backend messaging integration  

- **Login/Register Feature Improvements — Xiangyi Peng**  
  - Improved login/register UI UX  
  - Added loading states and future error handling placeholders  

- **User Profile + Map Feature (Backend) — Shenwei Zhang**  
  - User profile API implementation  
  - Map backend logic and data handling  
  - Provided updated models for UI integration  

- **Friend System UI — Enhui Zhao**  
  - Friend request list (incoming & outgoing)  
  - Friend list display  
  - Implemented FriendUiState & related data models  

---

## 4. Work Summary — Week 12

### Implemented Features
- **Chat Polish & Integration — Liye Fu**  
  - Merged chat branch to main with conflict resolution  
  - Polished message/thread UI; prepared resend and delivery markers  

- **Map & Comment UI Iteration — Xiangyi Peng**  
  - MapBox hooks and add-post entry points refined  
  - Comment presentation improved for consistency with chat  

- **Profile & Map Backend Hardening — Shenwei Zhang**  
  - Profile API validation and map statistics tuning  
  - Defensive checks on map post lifecycle  

- **Auth & Friend UX Updates — Enhui Zhao**  
  - Login/register UX refinements and error states  
  - Friendlist updates aligned with backend data contract  

---

## 5. Work Summary — Week 13

### Implemented Features
- **Map Add-New-Post Finalization — Xiangyi Peng**  
  - Finalized add-new-post flow; locked visibility defaults  
  - Shared flow across map branches for consistency  

- **Backend Stability Tweaks — Shenwei Zhang**  
  - Small modifications for checkpoints and deployment stability  
  - Public API packaging for handoff  

- **Friend Flow Finishing — Enhui Zhao**  
  - Final friend-flow passes; integrated checkpoint-3 assets  

- **Development Mode & Packaging**  
  - Checkpoint-3 development mode enabled and validated  

---

## 6. Work Summary — Week 14

### Implemented Features
- **Final Map/Post QA — Xiangyi Peng**  
  - Verified add-post and visibility flows across merged branches  
  - Cleaned map/comment edge cases for release readiness  

- **Chat Stability Pass — Liye Fu**  
  - Smoked message flows end-to-end; aligned UI states with backend events  

- **Backend & Profile Touch-ups — Shenwei Zhang**  
  - Applied final small modifications for stability and deployment  
  - Confirmed public API packaging and auth/session behavior  

- **Friend & Auth Wrap-up — Enhui Zhao**  
  - Final friend-flow alignment post-merge  
  - Checked login/signup surfaces for regressions ahead of handoff  
---

## 7. Codebase Maintenance
Merged branch `Xiangyi_c2` → `main`  
Merged branch `Shenwei_user_profile` → `main` (conflicts kept from `Shenwei_user_profile`)  
Merged branch `Enhui_Friend` → `main`  
Merged branch `Liye_IntegratedChat` → `main`  
Merged branch `Shenwei_map_feature` → `main`  
Merged branch `Merge_liyue_enhiu` → `main`  
Merged branch `Merge_liyue_shenwei` → `main`  
Added and updated `README` on `main`  

---

## 8. Next Steps (Weeks 10-12)
- Week 10 → tighten Retrofit wiring for map/post/friend endpoints; hook add-post UI to live API; baseline smoke tests (Xiangyi)
- Week 11 → WebSocket messaging and friend updates end-to-end; delivery receipts, reconnect handling, and chat resend queue (Liye & Shenwei)
- Week 12 → finish profile/avatar edit flows with backend validation; ship privacy toggles; add ViewModel/unit tests for chat/map/profile (Enhui & Shenwei)
