package com.one.hackathonlatam.dic25equipo69.churninsight;

import com.one.hackathonlatam.dic25equipo69.churninsight.controller.PredictionController;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev") // Usar perfil de desarrollo para tests
class ChurninsightApplicationTests {

	@Autowired
	private ApplicationContext context;

	@Test
	void contextLoads() {
		// Verificar que el contexto se carga correctamente
		assertThat(context).isNotNull();
	}

	@Test
	void mainBeansAreLoaded() {
		// Verificar que los beans principales están disponibles
		assertThat(context.getBean(PredictionController.class)).isNotNull();
		assertThat(context.getBean(IPredictionService.class)).isNotNull();
	}

	@Test
	void applicationStartsSuccessfully() {
		// Verificar que la aplicación puede iniciarse sin errores
		ChurninsightApplication.main(new String[]{});
	}
}
