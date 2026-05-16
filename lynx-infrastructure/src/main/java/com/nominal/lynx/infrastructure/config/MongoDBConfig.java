package com.nominal.lynx.infrastructure.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDBConfig — Configuración de Spring Data MongoDB 🍃🍂
 *
 * Esta clase habilita:
 * 1. Conexión a MongoDB con configuración personalizada.
 * 2. Transacciones ACID multi-documento (MongoTransactionManager).
 * 3. Escaneo de repositorios en la capa de infrastructure.
 *
 * CRÍTICO para LYNX:
 * ─────────────────
 * Las transacciones son obligatorias para garantizar que cuando un lote
 * cambia de estado, el evento correspondiente se registra en el Ledger
 * de forma atómica. Si una operación falla, la otra se revierte.
 *
 * Requisito de MongoDB:
 * ────────────────────
 * Para que las transacciones funcionen, MongoDB debe estar corriendo
 * en modo Replica Set, incluso si es un solo nodo.
 *
 * En docker-compose.yml:
 * ─────────────────────
 * services:
 *   mongodb:
 *     image: mongo:7.0
 *     command: ["--replSet", "rs0"]
 *     environment:
 *       MONGO_INITDB_DATABASE: lynx
 *
 * Luego, inicializa el replica set:
 * docker exec -it <container_id> mongosh --eval "rs.initiate()"
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.nominal.lynx.infrastructure.output.persistence")
public class MongoDBConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/lynx}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:lynx}")
    private String databaseName;

    /**
     * Nombre de la base de datos.
     * Se lee desde application.yml o usa 'lynx' por defecto.
     */
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    /**
     * Cliente de MongoDB con configuración personalizada.
     *
     * IMPORTANTE:
     * ──────────
     * Si necesitas ajustar timeouts, pools de conexiones, etc.,
     * hazlo aquí con MongoClientSettings.builder().
     */
    @Override
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        return MongoClients.create(mongoClientSettings);
    }

    /**
     * Transaction Manager — El motor de transacciones ACID.
     *
     * CÓMO FUNCIONA:
     * ─────────────
     * Cuando un Use Case tiene @Transactional (en la capa de Application),
     * Spring usa este bean para coordinar las operaciones en MongoDB.
     *
     * Si cualquier operación dentro de la transacción falla, TODO se revierte.
     *
     * Ejemplo en Use Case:
     * ───────────────────
     * @Transactional
     * public BatchResponse changeStatus(UUID batchId, BatchStatus newStatus) {
     *     batchRepository.updateStatus(batchId, newStatus);  // Operación 1
     *     ledgerRepository.append(event);                    // Operación 2
     *     return response;
     * }
     *
     * Si append() falla → updateStatus() se revierte automáticamente.
     *
     * @param dbFactory Factory de la base de datos (inyectado por Spring)
     * @return Transaction Manager configurado
     */
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    /**
     * NOTA sobre Replica Sets en Desarrollo:
     * ──────────────────────────────────────
     * Si no quieres configurar un replica set en local (aunque es recomendado),
     * puedes desactivar temporalmente las transacciones comentando el @Bean
     * de transactionManager() y quitando @Transactional de los Use Cases.
     *
     * ⚠️ ADVERTENCIA: Sin transacciones, el Ledger puede quedar inconsistente.
     * Solo hazlo en desarrollo muy temprano. En cualquier demo o producción,
     * las transacciones son OBLIGATORIAS.
     */
}
