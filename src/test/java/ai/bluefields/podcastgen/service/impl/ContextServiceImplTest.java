package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Context;
import ai.bluefields.podcastgen.repository.ContextRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextServiceImplTest {

    @Mock
    private ContextRepository contextRepository;

    @InjectMocks
    private ContextServiceImpl contextService;

    private Context context;

    @BeforeEach
    void setUp() {
        context = new Context();
        context.setId(1L);
        context.setDescriptionText("Test Description");
        context.setSourceUrl("http://example.com");
    }

    @Test
    void getAllContexts_ShouldReturnAllContexts() {
        when(contextRepository.findAll()).thenReturn(Arrays.asList(context));

        List<Context> result = contextService.getAllContexts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescriptionText()).isEqualTo("Test Description");
        verify(contextRepository).findAll();
    }

    @Test
    void getContextById_WhenContextExists_ShouldReturnContext() {
        when(contextRepository.findById(1L)).thenReturn(Optional.of(context));

        Optional<Context> result = contextService.getContextById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getSourceUrl()).isEqualTo("http://example.com");
        verify(contextRepository).findById(1L);
    }

    @Test
    void createContext_ShouldSaveAndReturnContext() {
        when(contextRepository.save(any(Context.class))).thenReturn(context);

        Context result = contextService.createContext(context);

        assertThat(result.getDescriptionText()).isEqualTo("Test Description");
        verify(contextRepository).save(context);
    }

    @Test
    void updateContext_WhenContextExists_ShouldUpdateAndReturnContext() {
        when(contextRepository.findById(1L)).thenReturn(Optional.of(context));
        when(contextRepository.save(any(Context.class))).thenReturn(context);

        Context result = contextService.updateContext(1L, context);

        assertThat(result.getSourceUrl()).isEqualTo("http://example.com");
        verify(contextRepository).findById(1L);
        verify(contextRepository).save(context);
    }

    @Test
    void updateContext_WhenContextDoesNotExist_ShouldThrowException() {
        when(contextRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contextService.updateContext(1L, context))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Context");

        verify(contextRepository).findById(1L);
        verify(contextRepository, never()).save(any());
    }

    @Test
    void deleteContext_ShouldDeleteContext() {
        contextService.deleteContext(1L);

        verify(contextRepository).deleteById(1L);
    }
}
