package br.com.example.davidarchanjo.service.impl;

import br.com.example.davidarchanjo.builder.AppBuilder;
import br.com.example.davidarchanjo.exception.AppNotFoundException;
import br.com.example.davidarchanjo.model.domain.App;
import br.com.example.davidarchanjo.model.dto.AppDTO;
import br.com.example.davidarchanjo.repository.AppRepository;
import br.com.example.davidarchanjo.service.AppService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import org.junit.jupiter.api;
import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import java.util.Arrays;
import java.util.Collections;
import org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.Assertions.assertThatThrownBy;
import org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import java.util.HashSet;
import java.util.Set;

public class AppServiceImplTest {

	@Test
	@Tag("valid")
	public void newAppCreation() {

		Long expectedId = 1L;
		AppDTO mockDto = mock(AppDTO.class);
		App mockApp = mock(App.class);
		when(builder.build(mockDto, new App())).thenReturn(mockApp);
		when(mockApp.getId()).thenReturn(expectedId);
		when(repository.save(any(App.class))).thenReturn(mockApp);

		Long actualId = appService.createNewApp(mockDto);

		assertEquals(expectedId, actualId);
		verify(builder, times(1)).build(mockDto, new App());
		verify(repository, times(1)).save(mockApp);
	}

	@Test
	@Tag("invalid")
	public void newAppCreationWithNull() {

		assertThrows(NullPointerException.class, () -> appService.createNewApp(null));
		verify(builder, never()).build(any());
		verify(repository, never()).save(any(App.class));
	}

	@Test
	@Tag("boundary")
	public void newAppCreationFailure() {

		AppDTO mockDto = mock(AppDTO.class);
		App mockApp = mock(App.class);
		when(builder.build(mockDto, new App())).thenReturn(mockApp);
		when(repository.save(any(App.class))).thenThrow(new RuntimeException());

		assertThrows(RuntimeException.class, () -> appService.createNewApp(mockDto));
		verify(builder, times(1)).build(mockDto, new App());
		verify(repository, times(1)).save(mockApp);
	}

	@BeforeEach
	void setUp() {
		app = new App();
		app.setId(1L);
		appDTO = new AppDTO();

	}

	@Test
	@Tag("valid")
	void testWhenRepositoryReturnsListOfApps() {
		List<App> apps = Arrays.asList(app);
		Mockito.when(appRepository.findAll()).thenReturn(apps);

		Mockito.when(appBuilder.build(app)).thenReturn(Optional.of(appDTO));
		List<Optional<AppDTO>> appDTOS = appService.getAllApps();
		Assertions.assertEquals(apps.size(), appDTOS.size());
		Assertions.assertEquals(Optional.of(appDTO), appDTOS.get(0));
	}

	@Test
	@Tag("boundary")
	void testWhenRepositoryReturnsEmptyListOfApps() {
		Mockito.when(appRepository.findAll()).thenReturn(Collections.emptyList());
		List<Optional<AppDTO>> appDTOS = appService.getAllApps();
		Assertions.assertTrue(appDTOS.isEmpty());
	}

	@Test
	@Tag("invalid")
	void testWhenRepositoryThrowsException() {
		Mockito.when(appRepository.findAll()).thenThrow(new RuntimeException());
		Assertions.assertThrows(AppNotFoundException.class, () -> appService.getAllApps());
	}

	@BeforeEach
	public void setup() {
		app = new App();
		appDTO = new AppDTO();
	}

@Test
@Tag("valid")
public void getAppByIdWithValidId() {
    when(repository.findById(anyLong())).thenReturn(Optional.of(app));

    when(builder.build(app)).thenReturn(Optional.of(appDTO));
    Optional<AppDTO> result = appService.getAppById(1L);
    assertTrue(result.isPresent());
    assertEquals(appDTO, result.get());
}

@Test
@Tag("invalid")
public void getAppByIdWithInvalidId() {
    when(repository.findById(anyLong())).thenReturn(Optional.empty());
    Exception exception = assertThrows(AppNotFoundException.class, () -> {
        appService.getAppById(-1L);
    });
    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("No such App for id"));
}

	@Test
	@Tag("boundary")
	public void getAppByIdWithNullId() {
		assertThrows(NullPointerException.class, () -> {
			appService.getAppById(null);
		});
	}

	@BeforeEach
	void setUp() {

		dto = new AppDTO();
		dto.setId(1L);
		dto.setName("Test App");
		dto.setAuthor("Test Author");
		dto.setVersion("1.0");
		model = new App();
		model.setId(dto.getId());
		model.setName(dto.getName());
		model.setAuthor(dto.getAuthor());
		model.setVersion(dto.getVersion());
		when(builder.build(dto, model)).thenReturn(model);
		when(builder.build(model)).thenReturn(dto);
	}

@Test
@Tag("valid")
void testUpdateAppWithValidId() {

    when(repository.findById(1L)).thenReturn(Optional.of(model));
    when(repository.save(model)).thenReturn(model);

    Optional<AppDTO> result = appService.updateApp(1L, dto);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(dto);
}

@Test
@Tag("invalid")
void testUpdateAppWithInvalidId() {

    when(repository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> appService.updateApp(1L, dto)).isInstanceOf(AppNotFoundException.class).hasMessageContaining("No such App for id '1'");
}

@Test
@Tag("boundary")
void testUpdateAppWithNullDto() {

    when(repository.findById(any())).thenReturn(Optional.of(model));

    assertThatThrownBy(() -> appService.updateApp(1L, null)).isInstanceOf(NullPointerException.class);
}

	@Test
	@Tag("boundary")
	void testUpdateAppWithNullIdAndDto() {

		assertThatThrownBy(() -> appService.updateApp(null, null)).isInstanceOf(NullPointerException.class);
	}

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@Tag("valid")
	public void testDeleteAppByValidId() {
		Long id = 1L;
		doNothing().when(repository).deleteById(id);
		service.deleteAppById(id);
		verify(repository, times(1)).deleteById(id);
	}

	@Test
	@Tag("invalid")
	public void testDeleteAppByNonExistingId() {
		Long id = 1000L;
		doThrow(new AppNotFoundException("No such App for id")).when(repository).deleteById(id);
		Exception exception = assertThrows(AppNotFoundException.class, () -> service.deleteAppById(id));
		assertTrue(exception.getMessage().contains("No such App for id"));
	}

	@Test
	@Tag("boundary")
	public void testDeleteAppByNullId() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> service.deleteAppById(null));
		assertEquals("Id must not be null", exception.getMessage());
	}

	@Test
	@Tag("valid")
	public void testAppPopulation() {
		doAnswer(i -> i.getArguments()[0]).when(repository).save(any(App.class));
		service.populate();
		verify(repository, times(100)).save(any(App.class));
	}

	@Test
	@Tag("valid")
	public void testRandomnessOfAppPopulation() {
		ArgumentCaptor<App> appCaptor = ArgumentCaptor.forClass(App.class);
		doAnswer(i -> i.getArguments()[0]).when(repository).save(appCaptor.capture());
		service.populate();
		Set<App> set = new HashSet<>(appCaptor.getAllValues());
		assert (set.size() == 100);
	}

}