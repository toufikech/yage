package fr.faylixe.yage.cpu.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyByte;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static fr.faylixe.yage.utils.MockitoUtils.toAnswer;

import java.util.function.Consumer;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import fr.faylixe.yage.cpu.instruction.IExecutionContext;
import fr.faylixe.yage.cpu.instruction.IInstruction;
import fr.faylixe.yage.cpu.register.IRegisterProvider;
import fr.faylixe.yage.cpu.register.IRegisterProviderTest;
import fr.faylixe.yage.memory.IMemoryStream;
import fr.faylixe.yage.memory.IMemoryStreamTest;

/**
 * Basic interface for performing instruction set tests.
 * Such interface extends test rules from :
 * 
 * - {@link IMemoryStreamTest}
 * - {@link IInstructionStreamTest}
 * - {@link IRegisterProviderTest}
 * 
 * So test must be taking in consideration, memory and register
 * associated scenario when instruction is executed.
 * 
 * @author fv
 */
@TestInstance(Lifecycle.PER_CLASS)
public interface IInstructionSetTest extends IInstructionStreamTest, IMemoryStreamTest, IRegisterProviderTest {

	/** {@inheritDoc} **/
	@Override
	default IRegisterProvider getTestRegisterProvider() {
		return IRegisterProviderTest.createMockRegisterProvider();
	}

	/** {@inheritDoc} **/
	@Override
	default IMemoryStream getTestMemoryStream() {
		return null; // TODO : Create mock address bus.
	}

	/** {@inheritDoc} **/
	@Override
	default IInstructionStream getTestInstructionStream() {
		return null; // TODO : Create mock.
	}

	/**
	 * Build a mock {@link IExecutionContext} in order to execute the given
	 * <tt>instruction</tt>. The context delegate {@link IRegisterProvider},
	 * {@link IMemoryStream} and {@link IInstructionStream} interfaces call
	 * to concrete test instance. Does not aims to be overriden.
	 * 
	 * @param expectedOpcode Expected opcode.
	 * @param expectedCycle Expected cycle value.
	 * @param instruction Instruction to test.
	 * @param test Test to perform.
	 */
	default void performInstructionTest(
			final int expectedOpcode,
			final int expectedCycle,
			final IInstruction instruction,
			final Consumer<IExecutionContext> test) {
		assertNotNull(instruction);
		assertEquals(expectedOpcode, instruction.getOpcode());
		assertEquals(expectedCycle, instruction.getCycle());
		final IExecutionContext context = mock(IExecutionContext.class);
		bindMemoryStream(context, getTestMemoryStream());
		bindInstructionStream(context, getTestInstructionStream());
		bindRegisterProvider(context, getTestRegisterProvider());
		try {
			instruction.execute(context);
		}
		catch (final IllegalAccessException e) {
			fail(e);
		}
		test.accept(context);
	}

	/**
	 * Binds the given mock execution <tt>context</tt> to
	 * the given memory <tt>stream</tt>.
	 * 
	 * @param context Mock execution context to bind stream to.
	 * @param stream Memory stream to bind.
	 */
	static void bindMemoryStream(final IExecutionContext context, final IMemoryStream stream) {
		try {
			when(context.readByte(anyInt())).then(toAnswer(stream::readByte));
			when(context.readBytes(anyInt(), anyInt())).then(toAnswer(stream::readBytes));
			doAnswer(toAnswer(stream::writeByte)).when(context).writeByte(anyByte(), anyInt());
			doAnswer(toAnswer(stream::writeBytes)).when(context).writeBytes(any(), anyInt());
		}
		catch (final IllegalAccessException e) {
			fail(e);
		}
	}

	/**
	 * Binds the given mock execution <tt>context</tt> to
	 * the given instruction <tt>stream</tt>
	 * 
	 * @param context Mock execution context to bind stream to.
	 * @param stream Instruction stream to bind.
	 */
	static void bindInstructionStream(final IExecutionContext context, final IInstructionStream stream) {
		try {
			when(context.nextByte()).then(toAnswer(stream::nextByte));
			when(context.nextShort()).then(toAnswer(stream::nextShort));
			// Note : sendByte not already considered.
		}
		catch (final IllegalAccessException e) {
			fail(e);
		}
	}

	/**
	 * Binds the given mock execution <tt>context</tt> to
	 * the given register <tt>provider</tt>
	 * 
	 * @param context Mock execution context to bind provider to.
	 * @param provider Register provider to bind.
	 */
	static void bindRegisterProvider(final IExecutionContext context, final IRegisterProvider provider) {
		when(context.getRegister(any())).then(toAnswer(provider::getRegister));
		when(context.getFlagsRegister()).then(toAnswer(provider::getFlagsRegister));
		when(context.getExtendedRegister(any())).then(toAnswer(provider::getExtendedRegister));
	}

}
