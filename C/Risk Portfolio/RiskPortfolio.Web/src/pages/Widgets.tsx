import { useState } from 'react';
import { useWidgets, useDeleteWidget, useCreateWidget, useWidgetTypes } from '@/hooks/useWidgets';
import { WidgetRenderer } from '@/components/widgets';
import type { CreateWidgetRequest } from '@/types/widgets';

export default function Widgets() {
  const { data: widgets, isLoading } = useWidgets(true);
  const { data: widgetTypes, isLoading: typesLoading, error: typesError } = useWidgetTypes();
  const deleteWidget = useDeleteWidget();
  const createWidget = useCreateWidget();
  const [showAddModal, setShowAddModal] = useState(false);

  // Debug logging
  console.log('Widget Types:', widgetTypes);
  console.log('Types Loading:', typesLoading);
  console.log('Types Error:', typesError);

  const handleDelete = (id: string) => {
    if (confirm('Are you sure you want to delete this widget?')) {
      deleteWidget.mutate(id);
    }
  };

  const handleAddWidget = (type: string) => {
    console.log('handleAddWidget called with type:', type);
    console.log('Available widget types:', widgetTypes);
    
    const widgetType = widgetTypes?.find(t => t.type === type);
    console.log('Found widget type:', widgetType);
    
    if (!widgetType) {
      console.error('Widget type not found:', type);
      return;
    }

    const request: CreateWidgetRequest = {
      title: widgetType.type,
      type: widgetType.type,
      configuration: '{}',
      positionX: 0,
      positionY: widgets?.length || 0,
      width: widgetType.defaultWidth,
      height: widgetType.defaultHeight,
      displayOrder: widgets?.length || 0
    };

    console.log('Creating widget with request:', request);
    
    createWidget.mutate(request, {
      onSuccess: () => {
        console.log('Widget created successfully');
        setShowAddModal(false);
      },
      onError: (error) => {
        console.error('Failed to create widget:', error);
      }
    });
  };

  if (isLoading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center', color: '#64748b' }}>
        Loading widgets...
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem', maxWidth: '1400px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 'bold', color: 'white', marginBottom: '0.5rem' }}>
            Dashboard Widgets
          </h1>
          <p style={{ fontSize: '0.875rem', color: '#64748b' }}>
            Customize your dashboard with widgets
          </p>
        </div>
        <button
          onClick={() => setShowAddModal(true)}
          style={{
            padding: '0.625rem 1.25rem',
            backgroundColor: '#3b82f6',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            fontSize: '0.875rem',
            fontWeight: '600',
            cursor: 'pointer',
            transition: 'background-color 200ms'
          }}
          onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#2563eb'}
          onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#3b82f6'}
        >
          Add Widget
        </button>
      </div>

      {/* Widgets Grid */}
      {!widgets || widgets.length === 0 ? (
        <div style={{
          textAlign: 'center',
          padding: '4rem 2rem',
          border: '1px dashed rgba(148, 163, 184, 0.2)',
          borderRadius: '0.5rem'
        }}>
          <div style={{ fontSize: '1rem', color: '#64748b', marginBottom: '1rem' }}>
            No widgets yet
          </div>
          <button
            onClick={() => setShowAddModal(true)}
            style={{
              padding: '0.625rem 1.25rem',
              backgroundColor: '#3b82f6',
              color: 'white',
              border: 'none',
              borderRadius: '0.375rem',
              fontSize: '0.875rem',
              fontWeight: '600',
              cursor: 'pointer'
            }}
          >
            Add Your First Widget
          </button>
        </div>
      ) : (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))',
          gap: '1.5rem'
        }}>
          {widgets.map((widget) => (
            <div key={widget.id} style={{ minHeight: '300px' }}>
              <WidgetRenderer
                widget={widget}
                onDelete={() => handleDelete(widget.id)}
              />
            </div>
          ))}
        </div>
      )}

      {/* Add Widget Modal */}
      {showAddModal && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.7)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 50,
            padding: '1rem'
          }}
          onClick={() => setShowAddModal(false)}
        >
          <div
            style={{
              backgroundColor: '#12161F',
              borderRadius: '0.5rem',
              border: '1px solid rgba(148, 163, 184, 0.2)',
              maxWidth: '600px',
              width: '100%',
              maxHeight: '80vh',
              overflow: 'auto'
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <div style={{
              padding: '1.5rem',
              borderBottom: '1px solid rgba(148, 163, 184, 0.1)'
            }}>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: 'white' }}>
                Add Widget
              </h2>
            </div>

            <div style={{ padding: '1.5rem' }}>
              {typesLoading && (
                <div style={{ textAlign: 'center', color: '#64748b', padding: '2rem' }}>
                  Loading widget types...
                </div>
              )}
              {typesError && (
                <div style={{ textAlign: 'center', color: '#fb7185', padding: '2rem' }}>
                  Error loading widget types: {String(typesError)}
                </div>
              )}
              {!typesLoading && !widgetTypes?.length && (
                <div style={{ textAlign: 'center', color: '#64748b', padding: '2rem' }}>
                  No widget types available
                </div>
              )}
              <div style={{ display: 'grid', gap: '1rem' }}>
                {widgetTypes?.map((type) => (
                  <button
                    key={type.type}
                    onClick={(e) => {
                      console.log('Button clicked for type:', type.type);
                      e.preventDefault();
                      e.stopPropagation();
                      handleAddWidget(type.type);
                    }}
                    style={{
                      padding: '1rem',
                      textAlign: 'left',
                      backgroundColor: 'rgba(148, 163, 184, 0.05)',
                      border: '1px solid rgba(148, 163, 184, 0.1)',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      transition: 'all 200ms'
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.backgroundColor = 'rgba(59, 130, 246, 0.1)';
                      e.currentTarget.style.borderColor = '#3b82f6';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.backgroundColor = 'rgba(148, 163, 184, 0.05)';
                      e.currentTarget.style.borderColor = 'rgba(148, 163, 184, 0.1)';
                    }}
                  >
                    <div style={{ fontSize: '0.875rem', fontWeight: '600', color: 'white', marginBottom: '0.25rem' }}>
                      {type.type}
                    </div>
                    <div style={{ fontSize: '0.75rem', color: '#64748b' }}>
                      {type.description}
                    </div>
                    <div style={{ fontSize: '0.7rem', color: '#64748b', marginTop: '0.5rem' }}>
                      Category: {type.category}
                    </div>
                  </button>
                ))}
              </div>
            </div>

            <div style={{
              padding: '1rem 1.5rem',
              borderTop: '1px solid rgba(148, 163, 184, 0.1)',
              display: 'flex',
              justifyContent: 'flex-end'
            }}>
              <button
                onClick={() => setShowAddModal(false)}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: 'transparent',
                  color: '#64748b',
                  border: '1px solid rgba(148, 163, 184, 0.2)',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem',
                  fontWeight: '600',
                  cursor: 'pointer'
                }}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
