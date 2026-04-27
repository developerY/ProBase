# Walkthrough: Envelope-Based Spending Control

I have implemented the "Envelope-Based Spending Control" feature, which moves Seaweed from simple budget tracking to **real-time decision-time control**.

## Core Features

### 1. Isolated Spending Control Module
Created a completely decoupled library module `:applications:seaweed:features:spendingcontrol`. This ensures the logic is isolated and can be reused or tested independently.
- **Envelopes**: Define spending boundaries with specific monthly limits and priority levels.
- **Real-Time Decision Engine**: Evaluates transactions before they are saved, determining if they fit within the allocated budget.
- **Transaction Classifier**: Automatically maps merchants to categories and determines if a purchase is a NEED or a WANT.

### 2. Decision-Time Enforcement
Integrated an "Intervention Step" into the transaction flow.
- When saving a transaction, the system now intercepts the request.
- If a limit is exceeded, it triggers an **Intervention Flow** instead of just saving the data.

### 3. "Decline & Recover" UX
Implemented a reusable intervention UI.
- **Declined State**: If you try to spend more than your "Dining" envelope allows (set to $50 for this POC), the transaction is "declined" in the app.
- **Recovery Options**: You are presented with a dialog to "Approve Anyway" (Override), allowing the system to learn from your behavior while keeping you aware of the limit.

## How to Test the POC
1.  Navigate to **Add Transaction**.
2.  Enter a description like "Dinner at Starbucks".
3.  Enter an amount **greater than $50** (e.g., $60).
4.  Select "Dining" as the category (or let AI/Rules classify it).
5.  Click **Save**.
6.  Observe the **Intervention Dialog** appearing, warning you that the budget is exceeded.
7.  Click **Approve Anyway** to complete the transaction.

## Technical Details
- **Module**: `:applications:seaweed:features:spendingcontrol`
- **Engine**: `RealTimeDecisionEngine` logic handles the limit checks.
- **Orchestrator**: `InterventionFlowOrchestrator` manages the UI state and resolution logic.
- **Classification**: `RulesBasedClassifier` provides immediate merchant-to-category mapping.
