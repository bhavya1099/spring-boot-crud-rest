package br.com.example.davidarchanjo.service.impl;

import br.com.example.davidarchanjo.builder.AppBuilder;
import br.com.example.davidarchanjo.exception.AppNotFoundException;
import br.com.example.davidarchanjo.model.domain.App;
import br.com.example.davidarchanjo.model.dto.AppDTO;
import br.com.example.davidarchanjo.repository.AppRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Assertions.assertThrows;
import org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito.when;
import org.junit.jupiter.api;
import br.com.example.davidarchanjo.service.AppService;
import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.mockito.Mockito;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentMatchers.anyLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito.times;
import org.mockito.Mockito.verify;
import java.time.Duration;
import java.time.Instant;

public class AppServiceImplTest {

	@Test
	@Tag("valid")
	void testSuccessfulAppCreation() {
		AppDTO mockedDTO = new AppDTO();
		App mockedApp = new App();
		mockedApp.setId(1L);
		when(builder.build(any(AppDTO.class))).thenReturn(mockedApp);
		when(repository.save(any(App.class))).thenReturn(mockedApp);
		Long result = service.createNewApp(mockedDTO);
		assertEquals(1L, result);
	}

@Test
@Tag("invalid")
void testAppBuildFailure() {
    when(builder.build(any(AppDTO.class))).thenThrow(AppNotFoundException.class);
    assertThrows(AppNotFoundException.class, () -> service.createNewApp(new AppDTO()));
}

@Test
@Tag("invalid")
void testRepositorySaveFailure() {
    when(builder.build(any(AppDTO.class))).thenReturn(new App());
    when(repository.save(any(App.class))).thenThrow(RuntimeException.class);
    assertThrows(RuntimeException.class, () -> service.createNewApp(new AppDTO()));
}

	@Test
	@Tag("invalid")
	void testNullAppDtoInput() {
		assertThrows(NullPointerException.class, () -> service.createNewApp(null));
	}

@Test
@Tag("boundary")
void testEmptyOptional() {
    when(builder.build(any(AppDTO.class))).thenAnswer(a -> new App());
    when(repository.save(any(App.class))).thenAnswer(a -> new App());
    Long result = service.createNewApp(Stream.empty().findFirst().orElse(null));
    assertEquals(null, result);
}

@Test
@Tag("valid")
public void testGetAllAppsWhenNoAppsPresent() {
    when(repository.findAll()).thenReturn(new ArrayList<>());
    List<Optional<AppDTO>> appList = appService.getAllApps();
    assertEquals(0, appList.size());
    verify(repository, times(1)).findAll();
}

	@Test
	@Tag("valid")
	public void testGetAllAppsWhenMultipleAppsPresent() {
		List<App> testApps = new ArrayList<>();
		List<Optional<AppDTO>> expectedDTOs = new ArrayList<>();

		App app1 = new App();

		App app2 = new App();
		testApps.add(app1);
		testApps.add(app2);

		AppDTO appDTO1 = new AppDTO();

		AppDTO appDTO2 = new AppDTO();
		expectedDTOs.add(Optional.of(appDTO1));
		expectedDTOs.add(Optional.of(appDTO2));
		when(repository.findAll()).thenReturn(testApps);
		when(builder.build(app1)).thenReturn(appDTO1);
		when(builder.build(app2)).thenReturn(appDTO2);
		List<Optional<AppDTO>> appList = appService.getAllApps();
		assertEquals(expectedDTOs.size(), appList.size());
		assertEquals(expectedDTOs, appList);
		verify(repository, times(1)).findAll();
		verify(builder, times(1)).build(app1);
		verify(builder, times(1)).build(app2);
	}

	@Test
	@Tag("valid")
	public void testGetAllAppsInvocationOrder() {
		List<App> testApps = new ArrayList<>();

		App app1 = new App();

		App app2 = new App();
		testApps.add(app1);
		testApps.add(app2);
		when(repository.findAll()).thenReturn(testApps);
		when(builder.build(Mockito.any(App.class))).thenReturn(new AppDTO());
		appService.getAllApps();
		InOrder inOrder = inOrder(repository, builder);
		inOrder.verify(repository).findAll();
		inOrder.verify(builder, times(testApps.size())).build(any(App.class));
	}

	@Tag("valid")
	@Test
	public void getAppByIdWithValidId() {
		Long id = 1L;
		App mockApp = new App();
		AppDTO expectedAppDTO = new AppDTO();
		when(repository.findById(id)).thenReturn(Optional.of(mockApp));
		when(builder.build(mockApp)).thenReturn(expectedAppDTO);
		Optional<AppDTO> actualAppDTO = appServiceImpl.getAppById(id);
		assertTrue(actualAppDTO.isPresent());
		assertEquals(expectedAppDTO, actualAppDTO.get());
		verify(repository, times(1)).findById(id);
		verify(builder, times(1)).build(mockApp);
	}

	@Tag("invalid")
	@Test
	public void getAppByIdWithInvalidId() {
		Long id = 2L;
		when(repository.findById(id)).thenReturn(Optional.empty());
		assertThrows(AppNotFoundException.class, () -> appServiceImpl.getAppById(id));
		verify(repository, times(1)).findById(id);
	}

	@Tag("boundary")
	@Test
	public void getAppByIdWithNullId() {
		assertThrows(NullPointerException.class, () -> appServiceImpl.getAppById(null));
	}

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	@Tag("valid")
	public void updateAppVersionWithKnownId() {

		App app = new App();
		app.setId(1L);
		AppDTO updatedAppDTO = new AppDTO();
		updatedAppDTO.setVersion("2.0.0");
		when(repository.findById(anyLong())).thenReturn(Optional.of(app));
		when(builder.build(any(AppDTO.class), any(App.class))).thenReturn(app);
		when(repository.save(any(App.class))).thenReturn(app);
		when(builder.build(any(App.class))).thenReturn(updatedAppDTO);

		Optional<AppDTO> resultDTO = service.updateApp(1L, updatedAppDTO);

		assertTrue(resultDTO.isPresent());
		assertEquals(updatedAppDTO.getVersion(), resultDTO.get().getVersion());
	}

	@Test
	@Tag("invalid")
	public void failToUpdateWithUnknownId() {

		AppDTO unknownAppDTO = new AppDTO();
		when(repository.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(AppNotFoundException.class, () -> {

			service.updateApp(2L, unknownAppDTO);
		});
	}

	@Test
	@Tag("boundary")
	public void updateWithNoChangesOnAppDTO() {

		App app = new App();
		app.setId(1L);
		AppDTO sameAppDTO = new AppDTO();
		when(repository.findById(anyLong())).thenReturn(Optional.of(app));
		when(builder.build(any(AppDTO.class), any(App.class))).thenReturn(app);
		when(repository.save(any(App.class))).thenReturn(app);
		when(builder.build(any(App.class))).thenReturn(sameAppDTO);

		Optional<AppDTO> resultDTO = service.updateApp(1L, sameAppDTO);

		assertTrue(resultDTO.isPresent());
		assertEquals(sameAppDTO, resultDTO.get());
	}

	@Test
	@Tag("integration")
	public void failToUpdateWhenRepositoryDown() {

		AppDTO anyAppDTO = new AppDTO();
		when(repository.findById(anyLong())).thenThrow(new RuntimeException("Repository is down"));

		assertThrows(RuntimeException.class, () -> {

			service.updateApp(1L, anyAppDTO);
		});
	}

	@Test
	@Tag("valid")
	void deleteAppByValidId() {
		Long validId = 1L;
		App app = new App();
		app.setId(validId);
		when(repository.findById(validId)).thenReturn(Optional.of(app));
		service.deleteAppById(validId);
		Assertions.assertThrows(AppNotFoundException.class, () -> service.getAppById(validId));
		verify(repository, times(1)).deleteById(validId);
	}

	@Test
	@Tag("invalid")
	void deleteAppByNonExistentId() {
		Long nonExistentId = 2L;
		when(repository.findById(nonExistentId)).thenReturn(Optional.empty());
		service.deleteAppById(nonExistentId);
		Assertions.assertThrows(AppNotFoundException.class, () -> service.getAppById(nonExistentId));
	}

	@Test
	@Tag("boundary")
	void deleteAppWithNullId() {
		Assertions.assertThrows(NullPointerException.class, () -> service.deleteAppById(null));
	}

	@BeforeEach
	public void setUp() {
		closeable = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	public void tearDown() throws Exception {
		closeable.close();
	}

	@Test
	@Tag("valid")
	public void populateMethodWithDataPopulation() {
		appService.populate();
		verify(appRepository, times(100)).save(any(App.class));
	}

@Test
@Tag("invalid")
public void populateMethodWithSaveFailure() {
    when(appRepository.save(any(App.class))).thenThrow(AppNotFoundException.class);
    try {
        appService.populate();
    } catch (AppNotFoundException ex) {

    }
    verify(appRepository, times(100)).save(any(App.class));
}

	@Test
	@Tag("boundary")
	public void populateMethodEfficiencyTest() {
		Instant start = Instant.now();
		appService.populate();
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();

		assertTrue(timeElapsed <= 100);
	}

}