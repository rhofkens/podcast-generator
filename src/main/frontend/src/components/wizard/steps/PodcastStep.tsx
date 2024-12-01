
import { useState, useEffect } from 'react';
import { PodcastGenerationWebSocket } from '../../../utils/websocket';

interface PodcastStepProps {
    podcastId: string | null;
    onBack: () => void;
    onComplete: () => void;
}

interface GenerationState {
    status: string;
    progress: number;
    message: string | null;
}

export function PodcastStep({ podcastId, onBack, onComplete }: PodcastStepProps) {
    const [generationState, setGenerationState] = useState<GenerationState>({
        status: '',
        progress: 0,
        message: null
    });
    const [error, setError] = useState<string | null>(null);
    const [consoleMessages, setConsoleMessages] = useState<string[]>([]);

    useEffect(() => {
        if (!podcastId) {
            setError('No podcast ID found');
            return;
        }

        let ws: PodcastGenerationWebSocket | null = null;

        const startGeneration = async () => {
            try {
                // Start podcast generation
                const response = await fetch(`/api/podcasts/${podcastId}/generate`, {
                    method: 'POST'
                });

                if (!response.ok) {
                    throw new Error('Failed to start podcast generation');
                }

                // Initialize WebSocket connection
                ws = new PodcastGenerationWebSocket(podcastId);
                
                ws.onMessage((data) => {
                    setGenerationState({
                        status: data.status,
                        progress: data.progress,
                        message: data.message
                    });
                    
                    // Add message to console log
                    setConsoleMessages(prev => [...prev, `[${data.status}] ${data.message}`]);
                    
                    if (data.status === 'COMPLETED') {
                        onComplete();
                    } else if (data.status === 'ERROR') {
                        setError(data.message || 'An error occurred during generation');
                    }
                });

            } catch (err) {
                setError(err instanceof Error ? err.message : 'Failed to start generation');
            }
        };

        startGeneration();

        // Cleanup
        return () => {
            if (ws) {
                ws.close();
            }
        };
    }, [podcastId, onComplete]);

    return (
        <div className="p-6">
            <div className="max-w-2xl mx-auto">
                <h2 className="text-2xl font-bold mb-4">Generating Podcast</h2>
                
                <div className="mb-4">
                    <div className="bg-black text-green-400 font-mono p-4 rounded-lg h-64 overflow-y-auto">
                        {consoleMessages.map((message, index) => (
                            <div key={index} className="whitespace-pre-wrap">
                                {`> ${message}`}
                            </div>
                        ))}
                    </div>
                </div>
                
                {error ? (
                    <div className="bg-red-50 text-red-500 p-4 rounded-lg mb-4">
                        {error}
                    </div>
                ) : (
                    <>
                        <div className="mb-4">
                            <div className="h-2 bg-gray-200 rounded-full">
                                <div 
                                    className="h-2 bg-primary rounded-full transition-all duration-500"
                                    style={{ width: `${generationState.progress}%` }}
                                />
                            </div>
                        </div>
                        
                        <p className="text-gray-600 mb-4">
                            {generationState.status || 'Starting generation...'}
                        </p>
                        
                        {generationState.message && (
                            <p className="text-sm text-gray-500">
                                {generationState.message}
                            </p>
                        )}
                    </>
                )}

                <div className="flex justify-between">
                    <button
                        onClick={onBack}
                        className="px-4 py-2 border rounded hover:bg-gray-50"
                    >
                        Back
                    </button>
                </div>
            </div>
        </div>
    );
}
