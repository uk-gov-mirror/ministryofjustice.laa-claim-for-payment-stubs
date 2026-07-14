package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Seeds reference data on application startup. */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MemberName")
public class ClaimsSeeder {

  private final DataSource dataSource;

  private static final String FILE_PATH = "db/data/claims.yaml";

  private static final AtomicBoolean HAS_RUN = new AtomicBoolean(false);

  @Bean
  @Profile("!prod")
  public ApplicationRunner seederRunner(ClaimsSeeder seeder) {
    return args -> seeder.seed();
  }

  /** Seeds the claims data on application startup. */
  public void seed() throws Exception {

    if (!HAS_RUN.compareAndSet(false, true)) {
      return;
    }

    log.info("Starting claims data seeding...");

    try (Connection connection = dataSource.getConnection()) {

      connection.setAutoCommit(false);

      try {
        clearTables(connection);

        ClaimsFile file = loadFile();
        assertSeedFileIsValid(file);

        insertClaims(connection, file);
        insertEvidence(connection, file);
        insertLineItems(connection, file);

        insertJoinRows(connection, file);

        insertDrafts(connection, file);

        connection.commit();
        log.info("Seeding completed successfully");

      } catch (Exception ex) {
        connection.rollback();
        log.error("Seeding failed, rolled back", ex);
        throw ex;
      }
    }
  }

  // ✅ Delete everything (safe re-run)
  private void clearTables(Connection connection) throws SQLException {
    log.info("Clearing existing data...");

    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate("DELETE FROM line_item_claim_evidence");
      stmt.executeUpdate("DELETE FROM claim_evidence");
      stmt.executeUpdate("DELETE FROM line_items");
      stmt.executeUpdate("DELETE FROM claims");
    }
  }

  private void insertClaims(Connection connection, ClaimsFile file) throws SQLException {

    String sql =
        "INSERT INTO claims (id, ufn, client, category, concluded, fee_type, escaped,"
            + " counsel_payment, claimed, provider_user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      for (ClaimRow c : file.claims) {
        ps.setObject(1, c.id);
        ps.setString(2, c.ufn);
        ps.setString(3, c.client);
        ps.setString(4, c.category);
        ps.setDate(5, Date.valueOf(c.concluded));
        ps.setString(6, c.feeType);
        ps.setBoolean(7, c.escaped);
        ps.setString(8, c.counselPayment);
        ps.setBigDecimal(9, c.claimed);
        ps.setObject(10, c.providerUserId);
        ps.executeUpdate();
      }
    }
  }

  private void insertEvidence(Connection connection, ClaimsFile file) throws SQLException {

    Map<String, Long> evidenceIds = new HashMap<>();
    String sql =
        "INSERT INTO claim_evidence"
            + " (id, claim_id, file_key, file_size, submitted_on) "
            + "VALUES (?, ?, ?, ?, ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      for (ClaimEvidenceRow e : file.claim_evidence) {
        ps.setObject(1, e.id);
        ps.setObject(2, e.claimId);
        ps.setString(3, e.fileIdString);
        ps.setLong(4, e.fileSize);
        ps.setTimestamp(5, Timestamp.from(e.submittedOn));
        ps.executeUpdate();
      }
    }
  }

  private void insertLineItems(Connection connection, ClaimsFile file) throws SQLException {

    Map<String, Long> lineItems = new HashMap<>();
    String sql =
        "INSERT INTO line_items (id, claim_id, title, category, date) VALUES (?, ?, ?, ?, ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      for (LineItemRow li : file.line_items) {

        ps.setObject(1, li.id);
        ps.setObject(2, li.claimId);
        ps.setString(3, li.title);
        ps.setString(4, li.category);
        ps.setDate(5, Date.valueOf(li.date));
        ps.executeUpdate();
      }
    }
  }

  private void insertJoinRows(Connection connection, ClaimsFile file) throws SQLException {

    String sql =
        "INSERT INTO line_item_claim_evidence (line_item_id, claim_evidence_id) VALUES (?, ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql)) {

      for (LineItemEvidenceRow link : file.line_item_claim_evidence) {

        ps.setObject(1, link.lineItemId);
        ps.setObject(2, link.evidenceId);
        ps.executeUpdate();
      }
    }
  }

  private ClaimsFile loadFile() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.registerModule(new JavaTimeModule());

    InputStream is =
        Optional.ofNullable(System.getenv("CLAIMS_SEED_FILE"))
            .map(
                path -> {
                  try {
                    return java.nio.file.Files.newInputStream(java.nio.file.Path.of(path));
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }
                })
            .orElseGet(
                () ->
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE_PATH));

    try (is) {
      return mapper.readValue(is, ClaimsFile.class);
    }
  }

  private void assertSeedFileIsValid(ClaimsFile file) {
    Set<String> keys = new HashSet<>();

    for (ClaimRow c : file.claims) {
      String key = c.ufn + "|" + c.client;
      if (!keys.add(key)) {
        throw new IllegalStateException("Duplicate claim: " + key);
      }
    }
  }

  private void insertDrafts(Connection connection, ClaimsFile file) throws SQLException {

    String sql =
        "INSERT INTO draft_claims (id, payload, provider_user_id) VALUES (?, CAST(? AS jsonb), ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      for (DraftClaimRow d : file.draft_claims) {
        ps.setObject(1, d.id);
        ps.setObject(2, d.payload);
        ps.setObject(3, d.providerUserId);
        ps.executeUpdate();
      }
    }
  }

  /** DTO for the claims data file. */
  public static class ClaimsFile {
    public List<ClaimRow> claims;
    public List<ClaimEvidenceRow> claim_evidence;
    public List<LineItemRow> line_items;
    public List<LineItemEvidenceRow> line_item_claim_evidence;
    public List<DraftClaimRow> draft_claims;
  }

  /** DTO for a claim row. */
  public static class ClaimRow {
    public UUID id;
    public String ufn;
    public String client;
    public String category;
    public String feeType;
    public String counselPayment;
    public LocalDate concluded;
    public boolean escaped;
    public BigDecimal claimed;
    public UUID providerUserId;
  }

  /** DTO for claim evidence. */
  public static class ClaimEvidenceRow {
    public UUID id;
    public UUID claimId;
    public String fileIdString;
    public Long fileSize;
    public Instant submittedOn;
  }

  /** DTO for a line item row. */
  public static class LineItemRow {
    public UUID id;
    public UUID claimId;
    public String title;
    public String category;
    public LocalDate date;
  }

  /** DTO for the link between line items and claim evidence. */
  public static class LineItemEvidenceRow {
    public UUID lineItemId;
    public UUID evidenceId;
  }

  /** DTO for a draft claim row. */
  public static class DraftClaimRow {
    public String id;
    public String payload;
    public String providerUserId;
  }
}
