package compiler.firm.backend.operations.templates;

import compiler.firm.backend.storage.RegisterBased;
import compiler.firm.backend.storage.Storage;

public interface SourceSourceDesinationOperationFactory {
	SourceSourceDestinationOperation instantiate(Storage source1, RegisterBased source2, RegisterBased destination);
}
