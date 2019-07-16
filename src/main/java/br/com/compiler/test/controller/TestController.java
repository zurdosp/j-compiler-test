package br.com.compiler.test.controller;

import java.io.PrintStream;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.mdkt.compiler.InMemoryJavaCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.compiler.test.model.ResponseTestCode;
import io.swagger.annotations.Api;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/dev-code-test-platform/test/v1")
@Api(tags = "Teste code", description = "API for test code process")
public class TestController {

	private static final Logger logger = LoggerFactory.getLogger(TestController.class);

	@PostMapping("/test")
	@ResponseBody
	public ResponseTestCode applyTest(@RequestBody String sourceCode, @RequestBody String sourceTest) throws Exception {

		try {
			StringBuffer sourceCodeBuffer = new StringBuffer();
			sourceCodeBuffer.append("package br.com.compiler.test.controller;\n");
			sourceCodeBuffer.append("public class MainTest1 {\n");
			sourceCodeBuffer.append("   public static void main(String args[]) { System.out.println(\"will\"); }");
			sourceCodeBuffer.append("}");

			Class<?> testClass = InMemoryJavaCompiler.newInstance().compile("br.com.compiler.test.controller.MainTest1", sourceCode);

			Assert.assertNotNull(testClass);
			Assert.assertEquals(1, testClass.getDeclaredMethods().length);
			Method sumInstanceMethod = testClass.getMethod("main", String[].class);
			Object[] obj = new Object[1];
			sumInstanceMethod.invoke(testClass, obj);
		} catch (Exception e) {
			logger.error("Erro ao tentar compilar codigo ", e);
			//return e.getMessage().toString().split(",")[2].toString();
			return new ResponseTestCode();
		}

		PrintStream console = System.out;
		//return console.toString();
		return new ResponseTestCode();
	}

	@PostMapping("/test2")
	@ResponseBody
	public ResponseTestCode applyTest2(@RequestBody String sourceCode, @RequestBody String sourceTest) throws Exception {
		ResponseTestCode responseTestCode = new ResponseTestCode();
		return responseTestCode;
	}

}
