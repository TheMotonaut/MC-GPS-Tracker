
#ifndef __COMPONENT_HPP__
#define __COMPONENT_HPP__

struct MC_Component {
    public:
        virtual ~MC_Component(void);
        virtual void init(void);
        virtual void shutdown(void);
        virtual void step(void);
};

#endif /* __COMPONENT_HPP__ */
