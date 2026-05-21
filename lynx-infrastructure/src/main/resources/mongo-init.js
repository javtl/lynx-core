// ============================================================================
// mongo-init.js — Script de Inicialización de MongoDB para LYNX Core
// ============================================================================
// Este script se ejecuta al levantar MongoDB por primera vez.
// Define las colecciones con JSON Schema Validation para proteger el Ledger
// de datos basura.
//
// Ubicación esperada en Docker:
// /docker-entrypoint-initdb.d/mongo-init.js
//
// En docker-compose.yml:
// volumes:
//   - ./src/main/resources/scripts/mongo-init.js:/docker-entrypoint-initdb.d/mongo-init.js:ro
// ============================================================================

db = db.getSiblingDB('lynx');

// ============================================================================
// COLECCIÓN: batches
// ============================================================================
// Almacena el estado ACTUAL de cada lote.
// Solo el "ahora"; el historial vive en ledger_events.
// ============================================================================

db.createCollection("batches", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "sku", "current_status", "quantity", "unit", "created_at", "created_by"],
      properties: {
        _id: {
          bsonType: "binData",
          description: "UUID del lote (Binary Data subtype 4)"
        },
        sku: {
          bsonType: "string",
          description: "Código del producto (obligatorio)"
        },
        current_status: {
          enum: ["DRAFT", "ACTIVE", "HOLD", "COMPLETED", "DISPATCHED", "DISCARDED"],
          description: "Estado actual del ciclo de vida"
        },
        quantity: {
          bsonType: "double",
          minimum: 0,
          description: "Cantidad disponible (no puede ser negativa)"
        },
        unit: {
          bsonType: "string",
          description: "Unidad de medida (kg, L, ud, etc.)"
        },
        metadata: {
          bsonType: "object",
          description: "Shadow Schema: atributos dinámicos del bioproducto"
        },
        created_at: {
          bsonType: "date",
          description: "Fecha de creación (inmutable)"
        },
        created_by: {
          bsonType: "binData",
          description: "UUID del usuario que creó el lote"
        }
      }
    }
  }
});

// Índices para optimizar consultas frecuentes
db.batches.createIndex({ "sku": 1 });
db.batches.createIndex({ "current_status": 1 });
db.batches.createIndex({ "created_at": -1 }); // Más recientes primero

print("✅ Colección 'batches' creada con validación JSON Schema.");

// ============================================================================
// COLECCIÓN: ledger_events
// ============================================================================
// El corazón del sistema: el Ledger inmutable (Append-Only).
// Cada acción sobre un lote genera un documento nuevo aquí.
// NUNCA se actualiza ni se borra.
// ============================================================================

db.createCollection("ledger_events", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "batch_id", "action", "source", "description", "timestamp"],
      properties: {
        _id: {
          bsonType: "binData",
          description: "UUID del evento (Binary Data subtype 4)"
        },
        batch_id: {
          bsonType: "binData",
          description: "UUID del lote afectado (obligatorio para trazabilidad)"
        },
        action: {
          enum: ["CREATE", "STATUS_CHANGE", "QUANTITY_UPDATE", "QUALITY_CHECK", "DISPATCH"],
          description: "Qué acción se realizó sobre el lote"
        },
        source: {
          enum: ["USER", "SYSTEM", "API", "IOT_SENSOR"],
          description: "Quién o qué originó el evento"
        },
        description: {
          bsonType: "string",
          minLength: 1,
          description: "Descripción legible del evento"
        },
        metadata: {
          bsonType: "object",
          description: "Detalles adicionales del evento (delta, parámetros, etc.)"
        },
        timestamp: {
          bsonType: "date",
          description: "Cuándo ocurrió el evento (UTC)"
        }
      }
    }
  }
});

// Índice compuesto para auditorías rápidas por lote
// Consulta típica: "Dame todos los eventos del lote #xyz ordenados por tiempo"
db.ledger_events.createIndex({ "batch_id": 1, "timestamp": -1 });

// Índice para búsquedas globales por timestamp
db.ledger_events.createIndex({ "timestamp": -1 });

print("✅ Colección 'ledger_events' creada con validación JSON Schema.");

// ============================================================================
// CONFIGURACIÓN DE REPLICA SET (Necesario para transacciones ACID)
// ============================================================================
// MongoDB solo soporta transacciones multi-documento en Replica Sets.
// Para desarrollo local, creamos un Replica Set de un solo nodo.
// ============================================================================

// Este script se ejecuta automáticamente si usas la imagen oficial de MongoDB
// con la variable de entorno MONGO_INITDB_ROOT_USERNAME.
// Si no, ejecuta esto manualmente desde el shell:
//
// rs.initiate({
//   _id: "rs0",
//   members: [{ _id: 0, host: "localhost:27017" }]
// });

print("✅ Script de inicialización completado.");
print("LYNX Core MongoDB está listo.");