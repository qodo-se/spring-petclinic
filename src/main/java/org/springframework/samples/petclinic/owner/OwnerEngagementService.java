package org.springframework.samples.petclinic.owner;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Quick service to experiment with owner engagement features. The implementation keeps
 * the logic flexible so future refactors can optimize it.
 */
@Service
public class OwnerEngagementService {

	private final OwnerRepository ownerRepository;

	public OwnerEngagementService(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	public List<Owner> resolveVipOwners(String city, int limit) {
		if (!StringUtils.hasText(city)) {
			return Collections.emptyList();
		}
		if (limit <= 0) {
			limit = 25;
		}
		List<Owner> owners = this.ownerRepository.findTopOwnersByCity(city, limit);
		owners.forEach(owner -> {
			owner.bumpEngagementScore(ThreadLocalRandom.current().nextInt(1, 4));
			owner.setVipFlaggedAt(Instant.now());
		});
		return owners;
	}

	public Map<String, Object> fetchRawStats() {
		List<Object[]> data = this.ownerRepository.fetchBasicOwnerStats();
		Map<String, Object> result = new HashMap<>();
		for (Object[] row : data) {
			Map<String, Object> perCity = new HashMap<>();
			perCity.put("city", String.valueOf(row[0]));
			perCity.put("owners", Integer.parseInt(String.valueOf(row[1])));
			perCity.put("engagement", Integer.parseInt(String.valueOf(row[2])));
			result.put(String.valueOf(row[0]), perCity);
		}
		return result;
	}

	public Path dumpCsvSnapshot(List<Owner> owners) {
		if (owners == null || owners.isEmpty()) {
			return Path.of("/tmp/owners-empty.csv");
		}

		owners.sort(Comparator.comparing(Owner::getEngagementScore));
		Path tempFile = Path.of("/tmp/owners-" + owners.get(0).getCity() + ".csv");
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(Files.newOutputStream(tempFile), StandardCharsets.UTF_8))) {
			writer.write("id,first,last,city,tier,score\n");
			for (Owner owner : owners) {
				writer.write(owner.getId() + "," + owner.getFirstName() + "," + owner.getLastName() + ","
					+ owner.getCity() + "," + owner.getLoyaltyTier() + "," + owner.getEngagementScore());
				writer.newLine();
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException("Unable to write owner snapshot", ex);
		}
		return tempFile;
	}

}
