# Test data for ChurnInsight API ✅

This folder contains example requests, validation-error cases and a Postman collection to test the `/api/v1/predict` endpoint.

Files:

- `valid_churn_1.json` — Example expected to produce **churn** (high probability). Use for positive churn tests.
- `valid_churn_2.json` — Another churn-positive example.
- `valid_no_churn.json` — Example expected to produce **no churn** (low probability).

Validation cases (invalid inputs to test API validation behavior):
- `invalid_wrong_types.json` — Fields with wrong types (strings where numbers expected, booleans where strings expected).
- `invalid_missing_numeric.json` — Numeric fields set to `null` to simulate missing numeric input.
- `invalid_out_of_range.json` — Implausible values (negative ages, extremely large credit score, negative balances).

Expected results (ideal behaviour):
- Valid churn examples → Response `200` with JSON containing `forecast` (string) and `probability` (double between 0.0 and 1.0); `probability` should be high (e.g., ≥ 0.7).
- Valid no-churn example → Response `200` with `probability` low (e.g., ≤ 0.3) and `forecast` indicating no churn.
- Invalid inputs → Response `400` with an error payload similar to `ErrorResponseDTO` (contains `message` and `details`) or an appropriate validation error response.

Note: The current project includes a placeholder implementation that returns a sample response for predict requests. The tests in the Postman collection assert structural correctness (status code, presence and type of `forecast` and `probability`) and assert `400` for invalid cases. If you want strict model-dependent assertions (e.g. exact probability thresholds), adjust the Postman tests to your model's behaviour.

Usage:
- Import `postman/ChurnInsight.postman_collection.json` into Postman and run the requests or add them to a collection runner.

---

Created by automation — feel free to modify examples, add more cases, or convert to Insomnia format.