# Datos de prueba para ChurnInsight API ✅

Esta carpeta contiene ejemplos de peticiones, casos de error de validación y una colección de Postman para probar el endpoint `/api/v1/predict`.

Archivos:

- `valid_churn_1.json` — Ejemplo que debería producir **churn** (alta probabilidad). Úsalo para pruebas con churn positivo.
- `valid_churn_2.json` — Otro ejemplo con churn esperado.
- `valid_no_churn.json` — Ejemplo que debería producir **no churn** (baja probabilidad).

Casos de validación (entradas inválidas para probar el comportamiento de validación):
- `invalid_wrong_types.json` — Campos con tipos incorrectos (strings donde se esperan números, booleanos donde se esperan strings).
- `invalid_missing_numeric.json` — Campos numéricos en `null` para simular entradas faltantes.
- `invalid_out_of_range.json` — Valores poco plausibles (edad negativa, creditScore extremadamente alto, balances negativos).

Resultados esperados (comportamiento ideal):
- Ejemplos válidos con churn → Respuesta `200` con JSON que contenga `forecast` (string) y `probability` (double entre 0.0 y 1.0); `probability` debería ser alta (p. ej., ≥ 0.7).
- Ejemplo válido sin churn → Respuesta `200` con `probability` baja (p. ej., ≤ 0.3) y `forecast` indicando no churn.
- Entradas inválidas → Respuesta `400` con un payload de error similar a `ErrorResponseDTO` (contiene `message` y `details`) o una respuesta de validación adecuada.

Nota: La implementación actual devuelve una respuesta de ejemplo para las peticiones `predict`. Las pruebas en la colección Postman comprueban la estructura (código de estado, presencia y tipo de `forecast` y `probability`) y esperan `400` para los casos inválidos. Si deseas aserciones estrictas dependientes del modelo (por ejemplo, umbrales exactos de probabilidad), ajusta las pruebas en Postman al comportamiento de tu modelo.

Uso:
- Importa `postman/ChurnInsight.postman_collection.json` en Postman y ejecuta las peticiones o úsalas en el Collection Runner.

---

Creado por automatización — siéntete libre de modificar ejemplos, añadir más casos o convertirlos al formato Insomnia.