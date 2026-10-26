I think this has genuine hackathon-winner potential because it turns AI glasses and a wearable charm into something emotionally engaging, not just another dashboard. The key is to make Meta Muse Charm a living representation of how well you're caring for your life — your health, your financial well-being, and the small decisions that connect them.

The winning demo should make judges feel that the charm is a companion they want to keep, not another app telling them what to do.

# Meta Muse Charm: Your Life, Growing With You

[starboy – CREATURE](https://images.openai.com/static-rsc-4/PStO9L2wkipcuNkywaU6Ciwsd2JLVtjfwidY4-WV0ZThLn1UjSet7kPygNOJup5kQoild44rjm4RqXyNkryECXd5j1otG4KNvFusOpOt9ZYSqcJmzKnkSPuDGDeSCyhjySEqKxsyOvbnhL_u2huXzTSNDBYOHIMtNil6D9zC2u8?purpose=inline)

[STARBOY pet digital: o acessório que une tecnologia e moda está em pré-venda - Hypnotique](https://images.openai.com/static-rsc-4/ddzUQqTaE3j4jjGXV4x6i4yoCFX3cZR3NmqrAmnCXipPnTg8Pls4XSvxmy16KcycYFR23VZ7UCHfaBspf0vNr4WpgTyDmXssckqlRnLHU9ox9hNFu0EG9go-gmHmWtO8jWO5gF7rz9QKYQog858HLzxbdE7bSSj6N7oNecr8yro?purpose=inline)

[マイクロペット パピー｜マイクロペット｜限定品や新作も。おもちゃ・グッズの通販ならタカラトミーモール【タカラトミー公式】](https://images.openai.com/static-rsc-4/iGp_XKsHReiKC5ILh_ZB8Cl-5GSuu7LUEvMxGh6FTc8ZEDAW-AZdH7xVX09YSIEVcV8GkBrRvwDbHMteoHKwq8e7ue_IHM5Mthi0xsd606sQGQzLzwX0pxeKlC_h_h7mBQExH3POirxdVuowBtnEBZBfiHMG21dbXhMZul9yKbU?purpose=inline)

11

The concept: Meta Muse becomes a personal AI life coach. Your Muse Charm reflects your real or simulated habits, responds to your decisions, and grows healthier as you build better routines.

- Health: Sleep, movement, hydration, recovery, and mindfulness.
- Finance: Saving, spending awareness, budgeting, and progress toward goals.
- AI coaching: Muse helps you choose a small, achievable next action.
- Living companion: The charm's appearance, expression, energy, and animations change in response to your progress.

The emotional hook is simple: You aren't just improving your life. You're helping your little companion thrive, too.

## 1. The experience that could win the demo

Build one short, cinematic experience in which the judges see the companion transform because the user makes better choices.

[Cinematic Cute 3D Cartoon Creature Sets ChatGPT Image Prompt | PromptBase](https://images.openai.com/static-rsc-4/fVwi4CcgfQzMHXL2bLULG4PpjhywrL1pbs92EHsrBTE4TRfRZlXTXpESts52tg5xlUJrVn4oDZk7grhcVYvtpVFXn8ORcRAKrJoVAiARLWfggQGSBOiFLPnV_WqHyhm9NWI8jYQkcM5vsfCfsIhmouFD-vvFfamq2kCiD5InSOg?purpose=inline)

Morning — Muse is tired

Simulated data: 5.5 hours of sleep, little movement, and a stressful spending day yesterday.

The charm looks sleepy, its glow is dim, and Muse says: “Let's make today easier. Ten minutes outside and a simple spending plan?”

[Micropets Kitten Assorted Styles – Toyworld Australia](https://images.openai.com/static-rsc-4/JCg_Iajo4CdtpNeMSGizAonD5RvRX6xiPL_x3SrIdGoQZMcK1uqjH3W1tcBddAaHxE6gf7hvmw8VZL6yGWlw_39A4dlbydgePJ7QihDP0IfIwt8eRfDChD5-QjbIzmg2ZBLT4UOpDo7cgTh1clKp65UEqkBclzDh0Ch702C_qOw?purpose=inline)

Afternoon — Small wins

The demo user takes a walk, drinks water, and avoids an unnecessary purchase.

The charm brightens, animates, and becomes more expressive. Muse celebrates the progress without demanding perfection.

[GOLDY - Rainbow missions](https://images.openai.com/static-rsc-4/b7IfZ7rtuD3MU0yTpZWnW1iBVpG5BLAAp64BTHKSTspB0T6PcaW1GdVHI_VnjPwcjBQ2RdZmfJDYaL4C3G4_w85t8TnQUU8PNh71giA1jACeUkg8MzuLl4CZvU-Yd-cxHsoO8fsMm37ElKcxaq08FmrXCkW94s4tXj0EaSsqooQ?purpose=inline)

Evening — Muse thrives

The user completes a personal goal and reviews the day.

The charm reaches a new growth stage. Muse explains what improved, recognizes the effort, and suggests one achievable goal for tomorrow.

The trick is to demonstrate the same character across all three states. Its evolution should feel earned, not random.

## 2. How I'd architect the proof of concept

The official Meta Global AI Developer Hackathon runs October 26–November 4, 2026, and provides access to Meta's newest models through Meta Model API. Its published themes include both building for your world and pushing the frontier.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://dev.meta.ai\&sz=32)

Meta Model API

+1



Synthetic Life Data

Health metrics + financial activity + goals

Life State Engine

Validate data, calculate progress, determine companion state

Meta Muse / Muse Spark

Interpret context, choose a coaching action, generate a supportive response

Muse Charm Renderer

Expression, glow, animation, growth stage, voice response

One important distinction: the public Meta Model API is documented as an interface for calling models such as Muse Spark. Meta's announcement describes Muse Charm as a voice-interactive physical device, but that does not by itself establish a public API for controlling the actual charm's display or internal state.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://dev.meta.ai\&sz=32)

Meta Model API

+1



So I would build against two interchangeable outputs:

- Demo charm: A fabricated physical charm or a polished on-screen companion with controllable states.
- Muse integration: Call the documented Meta Model API for intelligence, and integrate actual Muse Charm hardware only if its developer interface is available to your team.

That keeps the demo deliverable even if hardware access proves difficult.

## 3. Make the companion's health meaningful

I would avoid a single score that implies health and wealth are the same thing. Instead, give the companion a transparent state engine with separate dimensions that influence its overall appearance.

[Tracyy🧸✨ - Lita Duo Game](https://images.openai.com/static-rsc-4/PD3QB1-ld3jGG_CbPnK3rqsv8bwCaTLNlCiH9ZIWOo2tZjLZDOAmuXp35VlvvL2VeJIIBA6467c6u06dfg6VBl1E4VMxER0QquKyAQ5jPNG1GixS5-0CaTltsVbawRl35pbg-ziT8DBAKGFWddWpJTwTDDuJ89CXYz7aOst-524?purpose=inline)

## Muse Charm simulator

Adjust the sample inputs to see how the prototype responds.

### Growing 🌱

Illustrative demo logic, not a clinical or financial assessment.

Sleep last night

#### 7 hours

3h

6h

8h

10h

Movement goal completed

#### 65%

0%

50%

100%

Financial goal progress

#### 70%

0%

50%

100%

Completed a mindfulness or reflection break

Wellness signals

### 83/100

Financial goal progress

### 70/100

MUSE'S SUGGESTED MESSAGE

“Look at you building good habits! Let's keep the momentum going without chasing perfection.”

This is a small interactive model of the experience, not a live API connection. The health and finance numbers are illustrative, and the formulas are only prototype rules.

### The rules that matter

- Progress over perfection. Missing a goal should never make the companion sick, punish the user, or imply a medical problem.
- Separate health and finance. Financial stress shouldn't lower a health assessment, and income shouldn't determine how healthy the companion looks.
- Reward controllable behaviors. Celebrate a walk, a planned purchase, or consistency with a personal goal rather than weight, account balance, or wealth.
- Keep the system explainable. The local engine calculates the state; Muse explains it and proposes a next action. The model should not invent metrics or silently change scores.

## 4. Fabricate realistic demo data

For the hackathon, use a fixed synthetic persona and a deterministic sequence of events so every presentation produces the same compelling result.

| Data source    | Sample fields                                                           |
| -------------- | ----------------------------------------------------------------------- |
| Health         | Sleep duration, step count, activity minutes, hydration goal            |
| Finance        | Monthly budget, planned spending, savings goal, discretionary purchases |
| Personal goals | Daily targets, completion events, streaks, user preferences             |
| Companion      | Vitality state, mood, evolution stage, last coaching action             |

For example, the initial fixture might describe a user who slept poorly and exceeded a discretionary spending target. The demo then feeds in a walk, a mindfulness break, and a decision to skip an unplanned purchase.

Important implementation detail: keep the fabricated data explicitly marked as synthetic in the interface and API payload. Don't suggest the data came from a real wearable, bank, or health record. Use no actual personal financial or health records in the hackathon demo.

## 5. Make the Meta API call count

The model should do more than generate a cute sentence. It should interpret the user's context and return a structured coaching decision.

A proposed response contract:

```
{
  "mood": "encouraging",
  "growth_stage": "growing",
  "suggested_action": {
    "category": "movement",
    "title": "Take a 10-minute walk"
  },
  "message": "A little movement can be a good reset.",
  "reasons": [
    "movement_goal_incomplete",
    "user_selected_gentle_coaching"
  ]
}
```

This is your application's proposed JSON schema, not a claim about Meta's native response format.

Call Muse Spark through the official [Meta Model API](https://dev.meta.ai/docs/overview?project_id=3419047751588389\&team_id=2431753403985778\&utm_source=chatgpt.com), validate the returned structured fields, and map them into the charm's animation state.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://dev.meta.ai\&sz=32)

Meta Model API



For the strongest technical demonstration, show that:

1. The model receives a compact synthetic life summary.
2. It selects a relevant coaching action based on the context.
3. The application validates the response and applies a state transition.
4. The companion visibly reacts to the outcome.

Don't let the model directly determine medical diagnoses, investment recommendations, or financial transactions.

## 6. The 10-day hackathon execution plan

Days 1–2: The simulation engine

Build deterministic synthetic health and finance fixtures, the life-state engine, and a repeatable demo script.

Days 3–4: Muse integration

Connect to Muse Spark, define the coaching contract, validate responses, and add fallback behavior if the API fails.

Days 5–6: Bring the charm to life

Implement consistent character artwork, expressions, glow, and at least three visible growth states. Fabricate a physical enclosure only if it won't compromise the software demo.

Days 7–8: The emotional reveal

Rehearse the before-and-after experience. Make the transformation obvious, and show exactly why it happened.

Days 9–10: Submission quality

Test the full flow, prepare a concise video, document the API integration, and rehearse the pitch with a backup demo path.

## My recommendation

Position this as a new kind of embodied AI life coach, not a health tracker with a Tamagotchi skin. The charm is the emotional interface; Muse is the intelligence; the deterministic engine is what makes the experience trustworthy and repeatable.

A strong pitch line would be:

> Meet Muse Charm: the little companion that grows as you grow. Powered by AI, it turns everyday decisions about your health, money, and well-being into small steps toward a better life.

One final strategic consideration: the published hackathon schedule puts kickoff on October 26 and submissions on November 4. The official event page also says applications close October 25, subject to filling the participant cap early. Confirm your team's acceptance and review the official rules before committing to the build.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://dev.meta.ai\&sz=32)

Meta Model API

Exactly. The bigger idea isn't a Tamagotchi that tracks your habits. It's personal superintelligence that helps you manage your life through real-time feedback.

# Muse Charm

PERSONAL SUPERINTELLIGENCE

Your life. Understood. In real time.

[friend | Bould Design | industrial design studio | san francisco](https://images.openai.com/static-rsc-4/FDVLZjUQjy5z0XnsZJzTscqUReADNhd2Z5lSHoiz60BnL79tjJ49NIaRibucd8i0gTnmoeuCBlF2iOpKgMUNFVtJsR5jlnk7r-Wf55JOaHnc-8rO4kqxOAc7BRhQxHtdV-SRDFY1GPAN4ljtORWmiedxwR9TnYyrvJSIZ08Ke5U?purpose=inline)

[LUMI — AI Life Operating System :: Behance](https://images.openai.com/static-rsc-4/KfhY8-qJOG9bsqN-0b2qjSYfedXlU8n6rTNInKD60OnqduUlyBewvmAjWSIxfMZju4uyUhPSQTM8agj4FuWnKllAIOPL6AtUPoL8jelamElAOL81sO-7suf98mFJKfCisydYFoFacWCR1a3CP4kU2MXhQcTTMTE-JRgROmGNCBU?purpose=inline)

[Blog - Latest Articles & Insights](https://images.openai.com/static-rsc-4/hZfIfxpOkp3rcxmAIEQRBoK68HcSEyEANS4FrDvehbC9-vM_edMME5yCYmbrzpSOmXh0srx4Ao_ojhVL4tBZn9Q5XVDL30bxjfUsPyVFH99BN_Lgcy0Qz_7Ox6DPLFkGwfFQYLwO6IFf3zAv3ggf3QA7_nUb3kEjp5oC9LcYYIA?purpose=inline)

12

Here's how I'd frame the concept:

- Superintelligence: Meta Muse interprets your life context across health, finances, habits, goals, and daily decisions.
- Real-time feedback: As new events happen, Muse gives timely guidance to help you make better decisions.
- Living companion: Your Muse Charm reflects progress through its mood, glow, expressions, and evolution.
- Continuous improvement: Muse learns your preferences, helps you prioritize, and adapts its coaching to your goals.

The magic is the feedback loop:

Sense → Understand → Coach → Act → Improve.

Health and finance are your first two domains, not the limits of the vision.

For the hackathon, I'd use this headline:

> Muse Charm: Personal Superintelligence for Everyday Life.
>
> An AI life coach that turns real-time insights into better decisions—and makes your progress come alive in a companion you can carry with you.

One important distinction: position it as intelligence that helps you manage your life, not AI that takes control away from you. The user stays in charge; Muse makes the next good decision easier to see.
